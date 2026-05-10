package cn.xybbz.music

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.utils.Log
import cn.xybbz.config.music.DownloadCacheCommonController
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.platform.ContextWrapper
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
    private val cacheDirectory = File(contextWrapper.applicationDirectory, CACHE_DIRECTORY_NAME)

    @Volatile
    private var currentSessionId: Long? = null

    init {
        createScope()
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs()
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

        return "http://localhost:19180/cache-play?id=${session.id}"
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
        updateCacheSizeFlow(cacheDirectory.walkTopDown().filter { it.isFile }.sumOf { it.length() })
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

        var position = requestedStart.coerceAtLeast(0L)
        val endInclusive = requestedEndInclusive ?: (session.totalBytes - 1).coerceAtLeast(0L)
        val buffer = ByteArray(STREAM_BUFFER_SIZE)

        while (position <= endInclusive) {
            val readableEnd = waitForReadableEnd(session, position)
                ?: return if (position == requestedStart) CacheStreamResult.Unavailable else CacheStreamResult.Streamed

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
        val session = CacheSession(
            id = sessionId,
            cacheKey = cacheKey,
            url = music.musicUrl,
            totalBytes = music.size ?: 0L,
            tempFile = File(cacheDirectory, "$safeName.part"),
            finalFile = File(cacheDirectory, "$safeName.$extension"),
            contentType = contentTypeFor(extension),
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

                val contentType = response.headers[HttpHeaders.ContentType]
                if (!contentType.isNullOrBlank()) {
                    session.contentType = contentType
                }

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
            updateCacheSchedule(progress)
        }
    }

    private suspend fun waitForReadableEnd(session: CacheSession, position: Long): Long? {
        val startedAt = System.currentTimeMillis()
        while (System.currentTimeMillis() - startedAt < STREAM_WAIT_TIMEOUT_MS) {
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

    private fun safeFileName(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
            .replace("%", "_")
            .take(160)
            .ifBlank { "cache-${idGenerator.incrementAndGet()}" }
    }

    private fun contentTypeFor(extension: String): String {
        return when (extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "flac" -> "audio/flac"
            "m4a", "mp4" -> "audio/mp4"
            "aac" -> "audio/aac"
            "ogg", "oga" -> "audio/ogg"
            "wav" -> "audio/wav"
            else -> "application/octet-stream"
        }
    }

    private fun deleteQuietly(file: File) {
        runCatching {
            if (file.exists()) {
                file.delete()
            }
        }
    }

    override fun close() {
        cancelAllCache()
        super.close()
    }

    companion object {
        private const val CACHE_DIRECTORY_NAME = "jvm-playback-cache"
        private const val DOWNLOAD_BUFFER_SIZE = 64 * 1024
        private const val STREAM_BUFFER_SIZE = 64 * 1024
        private const val STREAM_WAIT_TIMEOUT_MS = 15_000L
        private const val STREAM_WAIT_INTERVAL_MS = 100L

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
}
