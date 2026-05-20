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
import io.ktor.utils.io.ByteReadChannel
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

/**
 * JVM 端播放缓存控制器。
 *
 * 这个控制器把 Media3 CacheDataSource 的思路翻译到 VLC/vlcj 可用的形态：
 * VLC 只访问本地 HTTP 地址，控制器负责在本地 span 缓存和上游 HTTP 之间选择数据来源。
 *
 * @property contextWrapper 平台上下文包装器，用于定位应用缓存目录。
 */
class JvmDownloadCacheController(
    private val contextWrapper: ContextWrapper,
) : DownloadCacheCommonController() {

    /**
     * 数据源管理器。
     *
     * 线程共享只读引用，用于拿到当前数据源配置过的 Ktor HttpClient。
     */
    private val dataSourceManager: DataSourceManager = get()

    /**
     * 当前进程内的播放缓存会话表。
     *
     * key 是本地代理 URL 中携带的 session id，value 是该次播放使用的缓存条目和预取任务。
     */
    private val sessions = ConcurrentHashMap<Long, CacheSession>()

    /**
     * 业务缓存 key 到 session id 的映射。
     *
     * 同一首歌重复播放时复用同一个 session，避免重复创建后台预取任务。
     */
    private val cacheKeyToSessionId = ConcurrentHashMap<String, Long>()

    /**
     * 本地代理 session id 生成器。
     *
     * 初始值使用当前时间戳，减少应用重启后短时间内 URL id 重复的概率。
     */
    private val idGenerator = AtomicLong(System.currentTimeMillis())

    /**
     * 默认 JVM 播放缓存目录。
     *
     * 当用户配置的自定义目录不可用时会回退到这个目录。
     */
    private val defaultCacheDirectory = File(contextWrapper.applicationDirectory, CACHE_DIRECTORY_NAME)
        .normalizedDirectory()

    /**
     * 当前实际使用的缓存目录。
     *
     * 这是线程共享可变属性，切换缓存目录时会取消内存 session 后整体替换。
     */
    @Volatile
    private var cacheDirectory = resolveInitialCacheDirectory()

    /**
     * 当前目录对应的 span 缓存存储层。
     *
     * 切换目录时会整体替换，控制器内所有缓存读写都通过它进行。
     */
    @Volatile
    private var cacheStore = JvmPlaybackCacheStore(cacheDirectory, DEFAULT_CONTENT_TYPE)

    /**
     * 当前正在播放或最近准备播放的 session id。
     *
     * 进度条只展示当前会话，避免后台预取其它歌曲时覆盖 UI。
     */
    @Volatile
    private var currentSessionId: Long? = null

    /**
     * 上一次缓存进度日志时间。
     *
     * 单位是毫秒，用于限制日志频率，避免高速写入 span 时刷屏。
     */
    @Volatile
    private var lastProgressLogTime = 0L

    init {
        createScope()
        if (!ensureCacheDirectory(cacheDirectory)) {
            cacheDirectory = defaultCacheDirectory
            ensureCacheDirectory(cacheDirectory)
        }
        cacheStore = JvmPlaybackCacheStore(cacheDirectory, DEFAULT_CONTENT_TYPE)
        settingsManager.updateCacheFilePath(cacheDirectory.absolutePath)
        observeCacheLimit()
        getCacheSize()
    }

    /**
     * 为 JVM 播放器准备本地缓存播放地址。
     *
     * 如果当前歌曲不满足缓存条件，返回 null，让播放控制器退回普通本地代理地址。
     *
     * @param music 当前准备播放的歌曲。
     * @return `/cache-play?id=` 形式的本地播放地址；不可缓存时返回 null。
     */
    fun preparePlaybackUrl(music: XyPlayMusic): String? {
        if (!isCacheable(music)) {
            return null
        }

        val session = synchronized(this) {
            val cacheKey = getCacheKey(music.itemId)
            val existingSession = cacheKeyToSessionId[cacheKey]?.let(sessions::get)
            val targetSession = existingSession ?: createSession(cacheKey, music)
            cacheStore.markAccessed(targetSession.entry)
            switchCurrentSession(targetSession.id)
            startDownloadIfNeeded(targetSession)
            targetSession
        }

        return JvmReverseProxyServer.wrapCachePlaybackUrl(session.id)
    }

    /**
     * 清空 JVM 播放缓存。
     *
     * 这里会取消所有内存任务，并删除 span index、span 文件以及旧版 `.part` 文件。
     */
    override fun clearCache() {
        cancelInMemorySessions(markStatus = CacheStatus.CANCELLED)
        cacheStore.deleteAll()
        currentSessionId = null
        updateCacheSchedule(0f)
        updateCacheSizeFlow(0L)
    }

    /**
     * 统计当前播放缓存目录占用空间。
     *
     * 统计结果包含 index.json、span 文件和可能残留的旧格式文件。
     */
    override fun getCacheSize() {
        updateCacheSizeFlow(cacheStore.sizeBytes())
    }

    /**
     * 按公共控制器约定缓存音乐。
     *
     * JVM 端只有静态音频才进入边播边缓存路径，因此非静态请求直接忽略。
     *
     * @param music 要缓存的歌曲。
     * @param ifStatic 调用方判断出的歌曲是否为静态资源。
     */
    override fun cacheMedia(
        music: XyPlayMusic,
        ifStatic: Boolean,
    ) {
        if (!ifStatic) {
            return
        }
        preparePlaybackUrl(music)
    }

    /**
     * 取消所有活跃缓存任务，但保留已经落盘的 span。
     *
     * 这和 clearCache 不同，适合用户暂停播放或切换状态时停止后台工作。
     */
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

    /**
     * 根据业务缓存 key 返回当前内存缓存会话。
     *
     * @param cacheKey 业务缓存 key。
     * @return 当前 session；不存在时返回 null。
     */
    override fun getMediaItem(cacheKey: String): Any? {
        return cacheKeyToSessionId[cacheKey]?.let(sessions::get)
    }

    /**
     * 当前 JVM 播放缓存目录路径。
     */
    override val cacheDirectoryPath: String
        get() = cacheDirectory.absolutePath

    /**
     * 当前缓存目录是否是平台默认目录。
     */
    override val isDefaultCacheDirectory: Boolean
        get() = cacheDirectory.isSameDirectory(defaultCacheDirectory)

    /**
     * 切换 JVM 播放缓存目录。
     *
     * @param path 用户选择的新目录路径。
     * @return true 表示目录可用且配置已持久化。
     */
    override suspend fun changeCacheDirectory(path: String): Boolean {
        val targetPath = path.trim().takeIf { it.isNotEmpty() } ?: return false
        return setActiveCacheDirectory(File(targetPath), persistAsDefault = false)
    }

    /**
     * 恢复 JVM 播放缓存默认目录。
     *
     * @return true 表示恢复成功。
     */
    override suspend fun restoreDefaultCacheDirectory(): Boolean {
        return setActiveCacheDirectory(defaultCacheDirectory, persistAsDefault = true)
    }

    /**
     * 将指定缓存会话的字节范围写给本地代理响应。
     *
     * 播放器请求 Range 时优先查 span 缓存；缺失区间立即回源，并把回源数据同步写入新的 span。
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

        cacheStore.markAccessed(session.entry)
        startDownloadIfNeeded(session)

        var position = requestedStart.coerceAtLeast(0L)
        val endInclusive = (requestedEndInclusive ?: (session.totalBytes - 1)).coerceAtMost(session.totalBytes - 1)
        val buffer = ByteArray(STREAM_BUFFER_SIZE)

        while (position <= endInclusive) {
            val cachedSpan = cacheStore.findSpanContaining(session.entry, position)
            if (cachedSpan != null) {
                // span 命中时只读取当前 span 能覆盖的部分，剩余范围继续循环判断，避免跨文件读取错误。
                val cachedEnd = minOf(cachedSpan.endInclusive, endInclusive)
                cacheStore.readSpanRange(
                    entry = session.entry,
                    span = cachedSpan,
                    start = position,
                    endInclusive = cachedEnd,
                    buffer = buffer,
                    output = output,
                )
                position = cachedEnd + 1
                continue
            }

            val nextCachedStart = cacheStore.nextSpanStartAfter(session.entry, position)
            val missingEnd = listOfNotNull(endInclusive, nextCachedStart?.minus(1))
                .minOrNull()
                ?: endInclusive

            // 缺失区间只回源到下一个已缓存 span 之前，避免重复下载已经命中的后续数据。
            val streamed = proxyRangeFromUpstreamWithCache(
                session = session,
                start = position,
                endInclusive = missingEnd,
                output = output,
                writeToCache = true,
            )
            if (!streamed) {
                return CacheStreamResult.Unavailable
            }
            position = missingEnd + 1
        }

        refreshSessionProgress(session)
        enforceCacheLimitIfNeeded()
        return CacheStreamResult.Streamed
    }

    /**
     * 获取本地代理生成响应头所需的会话快照。
     *
     * @param sessionId 缓存会话 id。
     * @return 只读快照；session 不存在时返回 null。
     */
    internal fun getSessionSnapshot(sessionId: Long): CacheSessionSnapshot? {
        val session = sessions[sessionId] ?: return null
        refreshSessionProgress(session)
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
     *
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
     * 从上游读取指定字节范围，写给播放器，并可选写入 span 缓存。
     *
     * 这是 Media3 cache miss 行为在 JVM 端的实现：播放请求不等后台预取，缺哪段就立即请求哪段。
     *
     * @param session 当前播放缓存会话。
     * @param start 上游补段读取的起始字节位置。
     * @param endInclusive 上游补段读取的结束字节位置。
     * @param output 本地代理响应输出通道；后台预取时为 null。
     * @param writeToCache true 表示把回源数据写入 span。
     * @return true 表示数据读取成功，false 表示上游无法提供该范围。
     */
    private suspend fun proxyRangeFromUpstreamWithCache(
        session: CacheSession,
        start: Long,
        endInclusive: Long,
        output: ByteWriteChannel?,
        writeToCache: Boolean,
    ): Boolean {
        if (start > endInclusive) {
            return true
        }

        val client = dataSourceManager.getHttpClient()
        return runCatching {
            val request = client.prepareGet(session.url) {
                // 上游 Range 是 span 缓存的基础；非 0 seek 如果拿不到 206，就不能保证写出的内容对应声明区间。
                header(HttpHeaders.Range, "bytes=$start-$endInclusive")
            }
            request.execute { response ->
                val status = response.status
                val acceptsFullBodyFallback = status == HttpStatusCode.OK && start == 0L
                if (status != HttpStatusCode.PartialContent && !acceptsFullBodyFallback) {
                    // 上游不支持非 0 Range 时不能降级成 200，否则 VLC 收到的字节和 Content-Range 会不一致。
                    return@execute false
                }

                updateSessionContentType(session, response.headers[HttpHeaders.ContentType])
                cacheStore.updateValidators(
                    entry = session.entry,
                    etag = response.headers[ETAG_HEADER],
                    lastModified = response.headers[LAST_MODIFIED_HEADER],
                )

                return@execute copyUpstreamRange(
                    session = session,
                    source = response.bodyAsChannel(),
                    output = output,
                    start = start,
                    endInclusive = endInclusive,
                    writeToCache = writeToCache,
                )
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
     * 复制上游响应体到播放器和 span 临时文件。
     *
     * write-through 让 seek 触发的前台回源也能落盘，下一次拖到同一位置就可以直接命中缓存。
     *
     * @param session 当前播放缓存会话。
     * @param source 上游 HTTP 响应体。
     * @param output 本地代理响应输出通道，后台预取时为 null。
     * @param start 当前复制范围起点。
     * @param endInclusive 当前复制范围终点。
     * @param writeToCache 是否写入本地 span 缓存。
     */
    private suspend fun copyUpstreamRange(
        session: CacheSession,
        source: ByteReadChannel,
        output: ByteWriteChannel?,
        start: Long,
        endInclusive: Long,
        writeToCache: Boolean,
    ): Boolean = withContext(Dispatchers.IO) {
        val bytesToCopy = endInclusive - start + 1
        val tempFile = if (writeToCache) {
            cacheStore.createTempSpanFile(session.entry, start, endInclusive)
        } else {
            null
        }
        val buffer = ByteArray(STREAM_BUFFER_SIZE)
        var remaining = bytesToCopy
        var copied = 0L

        try {
            val fileOutput = tempFile?.let { RandomAccessFile(it, "rw") }
            fileOutput.use { file ->
                while (remaining > 0L && !source.isClosedForRead) {
                    val readSize = minOf(buffer.size.toLong(), remaining).toInt()
                    val bytesRead = source.readAvailable(buffer, 0, readSize)
                    if (bytesRead == -1) {
                        break
                    }
                    if (bytesRead == 0) {
                        continue
                    }

                    output?.writeFully(buffer, 0, bytesRead)
                    output?.flush()
                    file?.write(buffer, 0, bytesRead)

                    copied += bytesRead
                    remaining -= bytesRead
                }
            }

            if (tempFile != null && copied == bytesToCopy) {
                // 临时文件完整写入后才提交 index，避免半段数据被后续请求误判为缓存命中。
                cacheStore.commitSpan(
                    entry = session.entry,
                    start = start,
                    endInclusive = endInclusive,
                    tempFile = tempFile,
                )
                refreshSessionProgress(session)
            } else {
                tempFile?.delete()
            }
            copied == bytesToCopy
        } catch (throwable: Throwable) {
            tempFile?.delete()
            throw throwable
        }
    }

    /**
     * 判断当前歌曲是否允许进入 JVM 边播边缓存。
     *
     * HLS 和本地文件不走 span 缓存，因为 HLS 有自己的分片协议，本地文件也不需要代理缓存。
     *
     * @param music 当前歌曲。
     * @return true 表示可以缓存。
     */
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

    /**
     * 创建新的播放缓存会话。
     *
     * 会话是进程内对象，真正可跨重启复用的是 entry 对应的 index.json 和 span 文件。
     *
     * @param cacheKey 业务缓存 key。
     * @param music 当前播放歌曲。
     * @return 新创建的缓存会话。
     */
    private fun createSession(
        cacheKey: String,
        music: XyPlayMusic,
    ): CacheSession {
        val sessionId = idGenerator.incrementAndGet()
        val safeName = safeFileName(cacheKey)
        val entry = cacheStore.openEntry(
            cacheKey = cacheKey,
            safeCacheKey = safeName,
            sourceUrl = music.musicUrl,
            totalBytes = music.size ?: 0L,
        )
        val session = CacheSession(
            id = sessionId,
            cacheKey = cacheKey,
            url = music.musicUrl,
            totalBytes = music.size ?: 0L,
            entry = entry,
            contentType = cacheStore.contentType(entry),
        )
        refreshSessionProgress(session)
        sessions[sessionId] = session
        cacheKeyToSessionId[cacheKey] = sessionId
        return session
    }

    /**
     * 切换当前播放会话。
     *
     * 前台播放优先级高于后台预取，切歌时取消上一首未完成预取，避免网络和磁盘写入争抢。
     *
     * @param sessionId 新的当前会话 id。
     */
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

    /**
     * 启动后台顺序预取任务。
     *
     * 如果会话已经完整缓存或任务正在运行，就不重复启动。
     *
     * @param session 当前播放缓存会话。
     */
    private fun startDownloadIfNeeded(session: CacheSession) {
        refreshSessionProgress(session)
        if (session.status == CacheStatus.COMPLETED || session.status == CacheStatus.DOWNLOADING) {
            return
        }
        val existingJob = session.job
        if (existingJob != null && existingJob.isActive) {
            return
        }

        session.job = scope.launch(Dispatchers.IO) {
            prefetchSession(session)
        }
    }

    /**
     * 后台预取当前歌曲的连续前缀。
     *
     * 预取只从 0 开始的连续缓存末尾向后推进；随机 seek 的远端缺口由前台播放请求即时回源。
     *
     * @param session 当前播放缓存会话。
     */
    private suspend fun prefetchSession(session: CacheSession) {
        session.status = CacheStatus.DOWNLOADING

        try {
            while (session.status != CacheStatus.CANCELLED) {
                refreshSessionProgress(session)
                if (session.status == CacheStatus.COMPLETED) {
                    return
                }

                val startOffset = cacheStore.firstMissingPositionFromStart(session.entry)
                if (startOffset >= session.totalBytes) {
                    refreshSessionProgress(session)
                    return
                }

                val endInclusive = (startOffset + PREFETCH_SPAN_BYTES - 1)
                    .coerceAtMost(session.totalBytes - 1)
                val fetched = proxyRangeFromUpstreamWithCache(
                    session = session,
                    start = startOffset,
                    endInclusive = endInclusive,
                    output = null,
                    writeToCache = true,
                )
                if (!fetched) {
                    // 后台预取失败不代表当前歌曲不可播；前台播放仍可通过普通回源路径继续尝试。
                    session.status = CacheStatus.PAUSED
                    return
                }
            }
        } catch (_: CancellationException) {
            if (session.status == CacheStatus.DOWNLOADING) {
                session.status = CacheStatus.PAUSED
            }
            throw CancellationException("Playback cache prefetch cancelled")
        } catch (throwable: Throwable) {
            session.status = CacheStatus.FAILED
            Log.e("jvm-cache", "播放缓存预取失败: ${session.url}", throwable)
        } finally {
            refreshSessionProgress(session)
            enforceCacheLimitIfNeeded()
            getCacheSize()
        }
    }

    /**
     * 刷新会话内存进度和状态。
     *
     * downloadedBytes 的语义是“span 去重后的已缓存字节数”，不是单个文件长度。
     *
     * @param session 当前播放缓存会话。
     */
    private fun refreshSessionProgress(session: CacheSession) {
        session.contentType = cacheStore.contentType(session.entry)
        session.downloadedBytes = cacheStore.cachedBytes(session.entry)
        if (cacheStore.isComplete(session.entry)) {
            session.status = CacheStatus.COMPLETED
        } else if (session.status == CacheStatus.COMPLETED) {
            session.status = CacheStatus.PAUSED
        }
        updateSessionProgress(session)
    }

    /**
     * 更新当前会话的 UI 缓存进度。
     *
     * @param session 当前播放缓存会话。
     */
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

    /**
     * 输出缓存进度日志。
     *
     * @param session 当前播放缓存会话。
     * @param progress 当前缓存进度，范围 0f..1f。
     */
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

    /**
     * 监听缓存上限配置变化。
     *
     * No 表示立即清空并禁用缓存，其它上限会触发一次 LRU 清理。
     */
    private fun observeCacheLimit() {
        scope.launch {
            settingsManager.cacheUpperLimit.collectLatest { limit ->
                if (limit == CacheUpperLimitEnum.No) {
                    clearCache()
                } else {
                    enforceCacheLimitIfNeeded()
                }
            }
        }
    }

    /**
     * 根据当前缓存上限执行 LRU 清理。
     *
     * 所有内存 session 的 cacheKey 都会被保护，避免 session 持有的 index 指向已删除的 span 文件。
     */
    private fun enforceCacheLimitIfNeeded() {
        val limitBytes = resolveCacheLimitBytes(settingsManager.get().cacheUpperLimit)
        if (limitBytes <= 0L) {
            return
        }
        val protectedKeys = sessions.values.map { session -> session.cacheKey }.toSet()
        cacheStore.enforceLimit(
            limitBytes = limitBytes,
            protectedCacheKeys = protectedKeys,
        )
        getCacheSize()
    }

    /**
     * 解析缓存上限枚举为字节数。
     *
     * JVM 端不再提供自动上限；历史 Auto 配置由平台映射按默认 2GB 处理。
     *
     * @param limit 当前缓存上限配置。
     * @return 最大缓存字节数；0 表示不执行 LRU，只由 No 清空。
     */
    private fun resolveCacheLimitBytes(limit: CacheUpperLimitEnum): Long {
        return limit.jvmCacheLimitBytes()
    }

    /**
     * 解析初始缓存目录。
     *
     * 用户没有配置自定义目录时使用平台默认目录。
     *
     * @return 初始缓存目录。
     */
    private fun resolveInitialCacheDirectory(): File {
        return settingsManager.get()
            .cacheFilePath
            .trim()
            .takeIf { it.isNotEmpty() }
            ?.let { File(it).normalizedDirectory() }
            ?: defaultCacheDirectory
    }

    /**
     * 设置当前活跃缓存目录。
     *
     * @param directory 目标目录。
     * @param persistAsDefault true 表示写入空路径，后续继续使用默认目录。
     * @return true 表示切换成功。
     */
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

    /**
     * 切换缓存目录的内存状态。
     *
     * 切目录会取消当前 session，因为旧 session 持有的是旧目录下的缓存 entry。
     *
     * @param directory 新缓存目录。
     */
    private fun switchCacheDirectory(directory: File) {
        synchronized(this) {
            cancelInMemorySessions(markStatus = CacheStatus.CANCELLED)
            cacheDirectory = directory
            cacheStore = JvmPlaybackCacheStore(directory, DEFAULT_CONTENT_TYPE)
            settingsManager.updateCacheFilePath(directory.absolutePath)
            updateCacheSchedule(0f)
        }
        getCacheSize()
    }

    /**
     * 取消并清空当前进程内所有 session。
     *
     * @param markStatus 要写入 session 的最终内存状态。
     */
    private fun cancelInMemorySessions(markStatus: CacheStatus) {
        sessions.values.forEach { session ->
            session.job?.cancel()
            session.status = markStatus
        }
        sessions.clear()
        cacheKeyToSessionId.clear()
        currentSessionId = null
    }

    /**
     * 把业务 key 转成安全文件名。
     *
     * @param value 原始缓存 key。
     * @return 可作为目录名使用的字符串。
     */
    private fun safeFileName(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
            .replace("%", "_")
            .take(160)
            .ifBlank { "cache-${idGenerator.incrementAndGet()}" }
    }

    /**
     * 使用上游响应头更新缓存会话的真实媒体类型。
     *
     * @param session 当前播放缓存会话。
     * @param contentType 上游 HTTP 响应返回的 Content-Type。
     */
    private fun updateSessionContentType(
        session: CacheSession,
        contentType: String?,
    ) {
        cacheStore.updateContentType(session.entry, contentType)
        session.contentType = cacheStore.contentType(session.entry)
    }

    /**
     * 确认缓存目录可用。
     *
     * @param directory 要检查或创建的目录。
     * @return true 表示目录存在且是目录。
     */
    private fun ensureCacheDirectory(directory: File): Boolean {
        return runCatching {
            if (directory.exists()) {
                directory.isDirectory
            } else {
                directory.mkdirs()
            }
        }.getOrDefault(false)
    }

    /**
     * 规范化目录路径。
     *
     * @return canonicalFile 失败时退回 absoluteFile。
     */
    private fun File.normalizedDirectory(): File {
        return runCatching {
            canonicalFile
        }.getOrDefault(absoluteFile)
    }

    /**
     * 判断两个目录是否指向同一规范化路径。
     *
     * @param other 另一个目录。
     * @return true 表示路径相同。
     */
    private fun File.isSameDirectory(other: File): Boolean {
        return normalizedDirectory().absolutePath == other.normalizedDirectory().absolutePath
    }

    /**
     * 关闭控制器并取消后台缓存任务。
     */
    override fun close() {
        cancelAllCache()
        super.close()
    }

    companion object {
        /**
         * JVM 播放缓存默认目录名。
         */
        private const val CACHE_DIRECTORY_NAME = "jvm-playback-cache"

        /**
         * 上游还没返回真实 Content-Type 前使用的保守默认媒体类型。
         */
        private const val DEFAULT_CONTENT_TYPE = "application/octet-stream"

        /**
         * 本地代理和上游读取使用的缓冲区大小，单位是 byte。
         */
        private const val STREAM_BUFFER_SIZE = 64 * 1024

        /**
         * 后台预取单个 span 的最大大小，单位是 byte。
         *
         * 小 span 能让 index 更快可用，也避免预取任务长时间占住一个大请求。
         */
        private const val PREFETCH_SPAN_BYTES = 512 * 1024L

        /**
         * 本地代理返回缓存播放响应头前，等待上游真实 Content-Type 的最长时间，单位是毫秒。
         */
        private const val CONTENT_TYPE_WAIT_TIMEOUT_MS = 1_000L

        /**
         * 等待 Content-Type 时的轮询间隔，单位是毫秒。
         */
        private const val CONTENT_TYPE_WAIT_INTERVAL_MS = 50L

        /**
         * 缓存进度日志最小间隔，单位是毫秒。
         */
        private const val PROGRESS_LOG_INTERVAL_MS = 1_000L

        /**
         * 上游 ETag 响应头名称。
         *
         * 使用字符串常量避免不同 Ktor 版本对 HttpHeaders 常量命名不一致。
         */
        private const val ETAG_HEADER = "ETag"

        /**
         * 上游 Last-Modified 响应头名称。
         *
         * 使用字符串常量可以让缓存索引字段和 HTTP 标准头保持一致。
         */
        private const val LAST_MODIFIED_HEADER = "Last-Modified"

        /**
         * 从 Koin 中安全获取 JVM 缓存控制器。
         *
         * @return 当前 JVM 缓存控制器；非 JVM 实现或尚未初始化时返回 null。
         */
        fun controllerOrNull(): JvmDownloadCacheController? {
            return runCatching {
                object : org.koin.core.component.KoinComponent {}.get<DownloadCacheCommonController>()
                        as? JvmDownloadCacheController
            }.getOrNull()
        }
    }
}

/**
 * 本地代理生成响应头所需的缓存会话快照。
 *
 * @property id 缓存会话 id。
 * @property totalBytes 媒体总字节数，单位是 byte。
 * @property downloadedBytes 已缓存去重字节数，单位是 byte。
 * @property status 当前缓存会话状态。
 * @property contentType 当前已解析的媒体类型。
 */
internal data class CacheSessionSnapshot(
    val id: Long,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val status: CacheStatus,
    val contentType: String,
)

/**
 * JVM 播放缓存会话状态。
 */
internal enum class CacheStatus {
    /**
     * 已创建但尚未开始预取。
     */
    QUEUED,

    /**
     * 后台预取正在运行。
     */
    DOWNLOADING,

    /**
     * 后台预取已暂停，已落盘 span 保留。
     */
    PAUSED,

    /**
     * span 已完整覆盖整首媒体。
     */
    COMPLETED,

    /**
     * 上游请求或本地写入失败。
     */
    FAILED,

    /**
     * 会话已取消，不再提供播放缓存。
     */
    CANCELLED,
}

/**
 * 本地代理写出缓存流的结果。
 */
internal sealed interface CacheStreamResult {
    /**
     * 数据已成功写入代理响应。
     */
    data object Streamed : CacheStreamResult

    /**
     * 请求的 session 不存在。
     */
    data object NotFound : CacheStreamResult

    /**
     * session 存在但当前不可用。
     */
    data object Unavailable : CacheStreamResult
}

/**
 * 进程内播放缓存会话。
 *
 * @property id 本地代理 URL 使用的会话 id。
 * @property cacheKey 业务缓存 key。
 * @property url 上游真实播放地址。
 * @property totalBytes 媒体总字节数，单位是 byte。
 * @property entry 当前歌曲对应的 span 缓存条目。
 * @property contentType 当前媒体类型，会从上游响应头更新。
 * @property downloadedBytes 已缓存去重字节数，单位是 byte。
 * @property status 当前会话状态。
 * @property job 后台预取任务；前台回源不挂在这里。
 */
private data class CacheSession(
    val id: Long,
    val cacheKey: String,
    val url: String,
    val totalBytes: Long,
    val entry: JvmPlaybackCacheEntry,
    @Volatile var contentType: String,
    @Volatile var downloadedBytes: Long = 0L,
    @Volatile var status: CacheStatus = CacheStatus.QUEUED,
    @Volatile var job: Job? = null,
)
