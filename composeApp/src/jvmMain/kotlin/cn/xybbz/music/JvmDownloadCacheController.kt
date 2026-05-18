package cn.xybbz.music

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.utils.Log
import cn.xybbz.config.music.DownloadCacheCommonController
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.platform.ContextWrapper
import cn.xybbz.proxy.JvmReverseProxyServer
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.get
import java.io.File
import java.io.RandomAccessFile
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

class JvmDownloadCacheController(
    private val contextWrapper: ContextWrapper,
) : DownloadCacheCommonController() {

    private val dataSourceManager: DataSourceManager = get()
    private val sessions = ConcurrentHashMap<Long, CacheSession>()
    private val cacheKeyToSessionId = ConcurrentHashMap<String, Long>()
    private val idGenerator = AtomicLong(System.currentTimeMillis())
    private val defaultCacheDirectory = File(contextWrapper.applicationDirectory, CACHE_DIRECTORY_NAME)
        .normalizedDirectory()

    @Volatile
    private var cacheDirectory = resolveInitialCacheDirectory()

    @Volatile
    private var currentSessionId: Long? = null
    @Volatile
    private var lastProgressLogTime = 0L

    init {
        createScope()
        if (!ensureCacheDirectory(cacheDirectory)) {
            cacheDirectory = defaultCacheDirectory
            ensureCacheDirectory(cacheDirectory)
        }
        settingsManager.updateCacheFilePath(cacheDirectory.absolutePath)
        observeCacheLimit()
    }

    fun preparePlaybackUrl(music: XyPlayMusic): String? {
        if (!isCacheable(music)) {
            return null
        }

        val session = synchronized(this) {
            val cacheKey = getCacheKey(music.itemId)
            val existingSession = cacheKeyToSessionId[cacheKey]?.let(sessions::get)
            val targetSession = existingSession ?: createSession(cacheKey, music)
            switchCurrentSession(targetSession.id)
            startDownloadIfNeeded(targetSession)
            targetSession
        }

        return JvmReverseProxyServer.wrapCachePlaybackUrl(session.id)
    }

    override fun clearCache() {
        val snapshot = sessions.values.toList()
        snapshot.forEach { session ->
            session.job?.cancel()
            session.status = CacheStatus.CANCELLED
            deleteQuietly(session.tempFile)
            deleteQuietly(session.finalFile)
            sessions.remove(session.id)
            cacheKeyToSessionId.remove(session.cacheKey)
        }
        currentSessionId = null
        updateCacheSchedule(0f)
        updateCacheSizeFlow(0L)
    }

    override fun getCacheSize() {
        val directory = cacheDirectory
        updateCacheSizeFlow(
            if (directory.exists()) {
                directory.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            } else {
                0L
            }
        )
    }

    override fun cacheMedia(
        music: XyPlayMusic,
        ifStatic: Boolean,
    ) {
        if (!ifStatic) {
            return
        }
        preparePlaybackUrl(music)
    }

    override fun cancelAllCache() {
        sessions.values.forEach { session ->
            if (session.status != CacheStatus.COMPLETED) {
                session.job?.cancel()
                session.status = CacheStatus.PAUSED
            }
        }
        currentSessionId = null
        updateCacheSchedule(0f)
    }

    override fun getMediaItem(cacheKey: String): Any? {
        return cacheKeyToSessionId[cacheKey]?.let(sessions::get)
    }

    override val cacheDirectoryPath: String
        get() = cacheDirectory.absolutePath

    override val isDefaultCacheDirectory: Boolean
        get() = cacheDirectory.isSameDirectory(defaultCacheDirectory)

    override suspend fun changeCacheDirectory(path: String): Boolean {
        val targetPath = path.trim().takeIf { it.isNotEmpty() } ?: return false
        return setActiveCacheDirectory(File(targetPath), persistAsDefault = false)
    }

    override suspend fun restoreDefaultCacheDirectory(): Boolean {
        return setActiveCacheDirectory(defaultCacheDirectory, persistAsDefault = true)
    }

    /**
     * 将指定缓存会话的字节范围写给本地代理响应。
     * 优先读取已经落盘的缓存文件；播放器请求超前时从上游补齐当前响应范围。
     *
     * @param sessionId 缓存会话 id，由本地反向代理播放地址携带。
     * @param requestedStart 播放器请求的起始字节位置。
     * @param requestedEndInclusive 播放器请求的结束字节位置，null 表示读到媒体末尾。
     * @param output 本地代理响应的输出通道。
     * @return 当前缓存会话的流式写出结果。
     */
    internal suspend fun writeSessionTo(
        sessionId: Long,
        requestedStart: Long,
        requestedEndInclusive: Long?,
        output: ByteWriteChannel,
    ): CacheStreamResult {
        val session = sessions[sessionId] ?: return CacheStreamResult.NotFound
        if (session.status == CacheStatus.FAILED || session.status == CacheStatus.CANCELLED) {
            return CacheStreamResult.Unavailable
        }

        startDownloadIfNeeded(session)

        // 本地代理已根据 HTTP Range 算出响应范围，这里只负责从对应位置开始写出字节。
        var position = requestedStart.coerceAtLeast(0L)
        val endInclusive = requestedEndInclusive ?: (session.totalBytes - 1).coerceAtLeast(0L)
        val buffer = ByteArray(STREAM_BUFFER_SIZE)

        while (position <= endInclusive) {
            /*
             * 先短暂等待后台顺序缓存把当前位置写入本地文件。
             * 如果缓存还没追上，就按当前响应范围从上游补段，避免播放必须等整首歌缓存完成。
             */
            val readableEnd = waitForReadableEnd(session, position, CACHE_MISS_WAIT_TIMEOUT_MS)
                ?: return if (proxyRangeFromUpstream(session, position, endInclusive, output)) {
                    CacheStreamResult.Streamed
                } else {
                    CacheStreamResult.Unavailable
                }

            val chunkEnd = minOf(readableEnd, endInclusive)
            readFileRange(session.readableFile(), position, chunkEnd, buffer, output)
            position = chunkEnd + 1
        }

        return CacheStreamResult.Streamed
    }

    internal fun getSessionSnapshot(sessionId: Long): CacheSessionSnapshot? {
        val session = sessions[sessionId] ?: return null
        return CacheSessionSnapshot(
            id = session.id,
            totalBytes = session.totalBytes,
            downloadedBytes = session.downloadedBytes,
            status = session.status,
            contentType = session.contentType,
        )
    }

    /**
     * 尽量在本地代理返回响应头前拿到上游真实 Content-Type。
     * 播放链接可能是转码流，响应类型以服务端返回为准，不能依赖原始音乐容器。
     *
     * @param sessionId 缓存会话 id。
     */
    internal suspend fun awaitResolvedContentType(sessionId: Long) {
        val session = sessions[sessionId] ?: return
        if (session.contentType != DEFAULT_CONTENT_TYPE) {
            return
        }

        startDownloadIfNeeded(session)
        val startedAt = System.currentTimeMillis()
        while (
            session.contentType == DEFAULT_CONTENT_TYPE &&
            session.status != CacheStatus.FAILED &&
            session.status != CacheStatus.CANCELLED &&
            System.currentTimeMillis() - startedAt < CONTENT_TYPE_WAIT_TIMEOUT_MS
        ) {
            delay(CONTENT_TYPE_WAIT_INTERVAL_MS)
        }
    }

    /**
     * 从上游读取当前响应缺失的字节范围并直接写给播放器。
     * 这对应 Media3 CacheDataSource 的 cache miss 行为：播放不等待后台下载完整追上。
     * 补段数据不写入顺序缓存文件，避免破坏后台下载任务正在维护的连续落盘内容。
     *
     * @param session 当前播放缓存会话。
     * @param start 上游补段读取的起始字节位置。
     * @param endInclusive 上游补段读取的结束字节位置。
     * @param output 本地代理响应的输出通道。
     * @return true 表示补段数据已成功写出，false 表示上游无法提供该范围数据。
     */
    private suspend fun proxyRangeFromUpstream(
        session: CacheSession,
        start: Long,
        endInclusive: Long,
        output: ByteWriteChannel,
    ): Boolean {
        if (start > endInclusive) {
            return true
        }

        val client = dataSourceManager.getHttpClient()
        return runCatching {
            val request = client.prepareGet(session.url) {
                // 上游必须只返回播放器当前需要的小段数据，否则响应长度会和实际写出不一致。
                header(HttpHeaders.Range, "bytes=$start-$endInclusive")
            }
            request.execute { response ->
                // 大多数音频源会返回 206；只有从 0 开始且上游不支持 Range 时才允许 200。
                if (response.status != HttpStatusCode.PartialContent &&
                    !(response.status == HttpStatusCode.OK && start == 0L)
                ) {
                    return@execute false
                }

                updateSessionContentType(session, response.headers[HttpHeaders.ContentType])
                copyLimited(
                    source = response.bodyAsChannel(),
                    output = output,
                    bytesToCopy = endInclusive - start + 1,
                )
                true
            }
        }.onFailure { throwable ->
            Log.e(
                "jvm-cache",
                "播放缓存上游补段失败: id=${session.id}, range=$start-$endInclusive",
                throwable
            )
        }.getOrDefault(false)
    }

    /**
     * 限量复制上游响应体，确保写出的字节数不超过当前 HTTP 响应声明的 Content-Length。
     *
     * @param source 上游 HTTP 响应体读取通道。
     * @param output 本地代理响应输出通道。
     * @param bytesToCopy 最多允许复制的字节数。
     */
    private suspend fun copyLimited(
        source: io.ktor.utils.io.ByteReadChannel,
        output: ByteWriteChannel,
        bytesToCopy: Long,
    ) {
        val buffer = ByteArray(STREAM_BUFFER_SIZE)
        var remaining = bytesToCopy
        while (remaining > 0L && !source.isClosedForRead) {
            val readSize = minOf(buffer.size.toLong(), remaining).toInt()
            val bytesRead = source.readAvailable(buffer, 0, readSize)
            if (bytesRead == -1) {
                break
            }
            if (bytesRead == 0) {
                continue
            }
            output.writeFully(buffer, 0, bytesRead)
            // 及时刷新给 VLC，避免小片段已经写入 Ktor 缓冲区但播放器仍等待更多数据。
            output.flush()
            remaining -= bytesRead
        }
    }

    private fun isCacheable(music: XyPlayMusic): Boolean {
        val settings = settingsManager.get()
        return settings.ifEnableEdgeDownload &&
                settings.cacheUpperLimit != CacheUpperLimitEnum.No &&
                music.static &&
                !music.ifHls &&
                music.filePath.isNullOrBlank() &&
                music.musicUrl.isNotBlank() &&
                (music.size ?: 0L) > 0L
    }

    private fun createSession(cacheKey: String, music: XyPlayMusic): CacheSession {
        val sessionId = idGenerator.incrementAndGet()
        val extension = music.container?.takeIf { it.isNotBlank() } ?: "audio"
        val safeName = safeFileName(cacheKey)
        val directory = cacheDirectory
        val session = CacheSession(
            id = sessionId,
            cacheKey = cacheKey,
            url = music.musicUrl,
            totalBytes = music.size ?: 0L,
            tempFile = File(directory, "$safeName.part"),
            finalFile = File(directory, "$safeName.$extension"),
            contentType = DEFAULT_CONTENT_TYPE,
        )
        sessions[sessionId] = session
        cacheKeyToSessionId[cacheKey] = sessionId
        return session
    }

    private fun switchCurrentSession(sessionId: Long) {
        val previousSessionId = currentSessionId
        if (previousSessionId != null && previousSessionId != sessionId) {
            sessions[previousSessionId]?.let { previous ->
                if (previous.status == CacheStatus.DOWNLOADING) {
                    previous.job?.cancel()
                    previous.status = CacheStatus.PAUSED
                }
            }
            updateCacheSchedule(0f)
        }
        currentSessionId = sessionId
    }

    private fun startDownloadIfNeeded(session: CacheSession) {
        if (session.status == CacheStatus.COMPLETED || session.status == CacheStatus.DOWNLOADING) {
            return
        }
        val existingJob = session.job
        if (existingJob != null && existingJob.isActive) {
            return
        }

        session.job = scope.launch(Dispatchers.IO) {
            downloadSession(session)
        }
    }

    private suspend fun downloadSession(session: CacheSession) {
        val client = dataSourceManager.getHttpClient()
        session.status = CacheStatus.DOWNLOADING

        try {
            session.tempFile.parentFile?.mkdirs()
            if (session.finalFile.exists() && session.finalFile.length() >= session.totalBytes) {
                session.downloadedBytes = session.finalFile.length()
                session.status = CacheStatus.COMPLETED
                updateSessionProgress(session)
                return
            }

            var startOffset = session.tempFile.takeIf { it.exists() }?.length() ?: 0L
            session.downloadedBytes = startOffset
            updateSessionProgress(session)

            val request = client.prepareGet(session.url) {
                header(HttpHeaders.Range, "bytes=$startOffset-")
            }

            request.execute { response ->
                if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.PartialContent) {
                    session.status = CacheStatus.FAILED
                    return@execute
                }

                if (response.status == HttpStatusCode.OK && startOffset > 0L) {
                    startOffset = 0L
                    session.tempFile.delete()
                    session.downloadedBytes = 0L
                }

                updateSessionContentType(session, response.headers[HttpHeaders.ContentType])

                writeResponseToTempFile(
                    session = session,
                    startOffset = startOffset,
                    source = response.bodyAsChannel(),
                )
            }

            if (session.downloadedBytes >= session.totalBytes && session.status != CacheStatus.FAILED) {
                moveTempToFinal(session)
                session.status = CacheStatus.COMPLETED
                updateSessionProgress(session)
            }
        } catch (_: CancellationException) {
            if (session.status == CacheStatus.DOWNLOADING) {
                session.status = CacheStatus.PAUSED
            }
            throw CancellationException("Playback cache download cancelled")
        } catch (throwable: Throwable) {
            session.status = CacheStatus.FAILED
            Log.e("jvm-cache", "播放缓存下载失败: ${session.url}", throwable)
        } finally {
            getCacheSize()
        }
    }

    private suspend fun writeResponseToTempFile(
        session: CacheSession,
        startOffset: Long,
        source: io.ktor.utils.io.ByteReadChannel,
    ) {
        val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
        var currentBytes = startOffset

        RandomAccessFile(session.tempFile, "rw").use { output ->
            output.seek(startOffset)
            while (!source.isClosedForRead) {
                val bytesRead = source.readAvailable(buffer, 0, buffer.size)
                if (bytesRead == -1) {
                    break
                }
                if (bytesRead == 0) {
                    continue
                }

                output.write(buffer, 0, bytesRead)
                currentBytes += bytesRead
                session.downloadedBytes = currentBytes
                updateSessionProgress(session)
            }
        }
    }

    private fun moveTempToFinal(session: CacheSession) {
        if (!session.tempFile.exists()) {
            return
        }
        session.finalFile.parentFile?.mkdirs()
        if (session.finalFile.exists() && session.finalFile.length() == 0L) {
            session.finalFile.delete()
        }
        if (!session.tempFile.renameTo(session.finalFile)) {
            session.tempFile.copyTo(session.finalFile, overwrite = true)
            session.tempFile.delete()
        }
        session.downloadedBytes = session.finalFile.length()
    }

    private fun updateSessionProgress(session: CacheSession) {
        if (currentSessionId == session.id) {
            val progress = if (session.totalBytes > 0L) {
                session.downloadedBytes.toFloat() / session.totalBytes.toFloat()
            } else {
                0f
            }
            logCacheProgress(session, progress)
            updateCacheSchedule(progress)
        }
    }

    private fun logCacheProgress(
        session: CacheSession,
        progress: Float,
    ) {
        val now = System.currentTimeMillis()
        if (progress < 1f && now - lastProgressLogTime < PROGRESS_LOG_INTERVAL_MS) {
            return
        }
        lastProgressLogTime = now
        Log.i(
            "jvm-cache",
            "播放缓存进度 id=${session.id}, key=${session.cacheKey}, " +
                    "progress=${(progress * 100).coerceIn(0f, 100f)}%, " +
                    "downloaded=${session.downloadedBytes}, total=${session.totalBytes}"
        )
    }

    private suspend fun waitForReadableEnd(
        session: CacheSession,
        position: Long,
        timeoutMs: Long,
    ): Long? {
        val startedAt = System.currentTimeMillis()
        while (System.currentTimeMillis() - startedAt < timeoutMs) {
            val readableLength = session.readableFile().takeIf { it.exists() }?.length() ?: 0L
            if (readableLength > position) {
                return readableLength - 1
            }
            if (session.status == CacheStatus.COMPLETED && readableLength <= position) {
                return null
            }
            if (session.status == CacheStatus.FAILED || session.status == CacheStatus.CANCELLED) {
                return null
            }
            startDownloadIfNeeded(session)
            delay(STREAM_WAIT_INTERVAL_MS)
        }
        return null
    }

    private suspend fun readFileRange(
        file: File,
        start: Long,
        endInclusive: Long,
        buffer: ByteArray,
        output: ByteWriteChannel,
    ) = withContext(Dispatchers.IO) {
        RandomAccessFile(file, "r").use { input ->
            input.seek(start)
            var remaining = endInclusive - start + 1
            while (remaining > 0L) {
                val readSize = minOf(buffer.size.toLong(), remaining).toInt()
                val bytesRead = input.read(buffer, 0, readSize)
                if (bytesRead <= 0) {
                    break
                }
                output.writeFully(buffer, 0, bytesRead)
                // 缓存文件边增长边读时，要尽快把每块数据推给播放器。
                output.flush()
                remaining -= bytesRead
            }
        }
    }

    private fun observeCacheLimit() {
        scope.launch {
            settingsManager.cacheUpperLimit.collectLatest {
                if (it == CacheUpperLimitEnum.No) {
                    clearCache()
                }
            }
        }
    }

    private fun resolveInitialCacheDirectory(): File {
        return settingsManager.get()
            .cacheFilePath
            .trim()
            .takeIf { it.isNotEmpty() }
            ?.let { File(it).normalizedDirectory() }
            ?: defaultCacheDirectory
    }

    private suspend fun setActiveCacheDirectory(
        directory: File,
        persistAsDefault: Boolean,
    ): Boolean = withContext(Dispatchers.IO) {
        val targetDirectory = directory.normalizedDirectory()
        if (!ensureCacheDirectory(targetDirectory)) {
            return@withContext false
        }

        val storedPath = if (
            persistAsDefault ||
            targetDirectory.isSameDirectory(defaultCacheDirectory)
        ) {
            ""
        } else {
            targetDirectory.absolutePath
        }

        val persisted = runCatching {
            settingsManager.setCacheFilePath(storedPath)
        }.isSuccess
        if (!persisted) {
            return@withContext false
        }

        if (targetDirectory.isSameDirectory(cacheDirectory)) {
            settingsManager.updateCacheFilePath(targetDirectory.absolutePath)
            getCacheSize()
        } else {
            switchCacheDirectory(targetDirectory)
        }
        true
    }

    private fun switchCacheDirectory(directory: File) {
        synchronized(this) {
            cancelInMemorySessions()
            cacheDirectory = directory
            settingsManager.updateCacheFilePath(directory.absolutePath)
            updateCacheSchedule(0f)
        }
        getCacheSize()
    }

    private fun cancelInMemorySessions() {
        sessions.values.forEach { session ->
            session.job?.cancel()
            session.status = CacheStatus.CANCELLED
        }
        sessions.clear()
        cacheKeyToSessionId.clear()
        currentSessionId = null
    }

    private fun safeFileName(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
            .replace("%", "_")
            .take(160)
            .ifBlank { "cache-${idGenerator.incrementAndGet()}" }
    }

    /**
     * 使用上游响应头更新缓存会话的真实媒体类型。
     * 当前播放地址可能是转码流，不能用原始音乐容器推断 Content-Type。
     *
     * @param session 当前播放缓存会话。
     * @param contentType 上游 HTTP 响应返回的 Content-Type。
     */
    private fun updateSessionContentType(
        session: CacheSession,
        contentType: String?,
    ) {
        if (!contentType.isNullOrBlank()) {
            session.contentType = contentType
        }
    }

    private fun deleteQuietly(file: File) {
        runCatching {
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun ensureCacheDirectory(directory: File): Boolean {
        return runCatching {
            if (directory.exists()) {
                directory.isDirectory
            } else {
                directory.mkdirs()
            }
        }.getOrDefault(false)
    }

    private fun File.normalizedDirectory(): File {
        return runCatching {
            canonicalFile
        }.getOrDefault(absoluteFile)
    }

    private fun File.isSameDirectory(other: File): Boolean {
        return normalizedDirectory().absolutePath == other.normalizedDirectory().absolutePath
    }

    override fun close() {
        cancelAllCache()
        super.close()
    }

    companion object {
        private const val CACHE_DIRECTORY_NAME = "jvm-playback-cache"
        /**
         * 上游还没返回真实 Content-Type 前使用的保守默认媒体类型。
         */
        private const val DEFAULT_CONTENT_TYPE = "application/octet-stream"
        private const val DOWNLOAD_BUFFER_SIZE = 64 * 1024
        private const val STREAM_BUFFER_SIZE = 64 * 1024
        private const val CACHE_MISS_WAIT_TIMEOUT_MS = 300L
        private const val STREAM_WAIT_INTERVAL_MS = 100L
        /**
         * 本地代理返回缓存播放响应头前，等待上游真实 Content-Type 的最长时间。
         */
        private const val CONTENT_TYPE_WAIT_TIMEOUT_MS = 1_000L
        private const val CONTENT_TYPE_WAIT_INTERVAL_MS = 50L
        private const val PROGRESS_LOG_INTERVAL_MS = 1_000L

        fun controllerOrNull(): JvmDownloadCacheController? {
            return runCatching {
                object : org.koin.core.component.KoinComponent {}.get<DownloadCacheCommonController>()
                        as? JvmDownloadCacheController
            }.getOrNull()
        }
    }
}

internal data class CacheSessionSnapshot(
    val id: Long,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val status: CacheStatus,
    val contentType: String,
)

internal enum class CacheStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED,
}

internal sealed interface CacheStreamResult {
    data object Streamed : CacheStreamResult
    data object NotFound : CacheStreamResult
    data object Unavailable : CacheStreamResult
}

private data class CacheSession(
    val id: Long,
    val cacheKey: String,
    val url: String,
    val totalBytes: Long,
    val tempFile: File,
    val finalFile: File,
    @Volatile var contentType: String,
    @Volatile var downloadedBytes: Long = max(finalFile.length(), tempFile.length()),
    @Volatile var status: CacheStatus = CacheStatus.QUEUED,
    @Volatile var job: Job? = null,
) {
    fun readableFile(): File {
        return if (finalFile.exists()) finalFile else tempFile
    }

    /**
     * 获取当前播放器可直接读取的本地缓存文件长度。
     *
     * @return 已完成文件或临时缓存文件当前可读长度，不存在时返回 0。
     */
    fun readableLength(): Long {
        return readableFile().takeIf { it.exists() }?.length() ?: 0L
    }
}
