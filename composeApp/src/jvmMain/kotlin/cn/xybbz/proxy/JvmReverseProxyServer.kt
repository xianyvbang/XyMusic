package cn.xybbz.proxy

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.music.CacheStatus
import cn.xybbz.music.CacheStreamResult
import cn.xybbz.music.HlsResourceKind
import cn.xybbz.music.HlsResourceStreamResult
import cn.xybbz.music.JvmDownloadCacheController
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.content.OutgoingContent
import io.ktor.http.takeFrom
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.head
import io.ktor.server.routing.options
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.copyTo
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * JVM 端本地反向代理服务。
 * 该服务固定监听 localhost:19180，并将请求按原始方法、头信息与请求体转发到目标地址。
 */
object JvmReverseProxyServer : KoinComponent {

    private const val PROXY_HOST = "localhost"
    private const val PROXY_PORT = 19180
    /**
     * 缓存未完成时，每次 HTTP Range 响应最多承诺给播放器的字节窗口。
     */
    private const val CACHE_STREAM_WINDOW_BYTES = 128 * 1024L
    private val proxyWorkerThreadCount =
        Runtime.getRuntime().availableProcessors().coerceAtLeast(4)
    private val proxyCallThreadCount = (proxyWorkerThreadCount * 2).coerceAtLeast(8)
    private val proxyRunningRequestLimit = (proxyCallThreadCount * 16).coerceAtMost(256)

    /**
     * 需要从下游请求中过滤掉的受限请求头。
     * 这些头要么由 Ktor/Netty 自动管理，要么属于 hop-by-hop 头，不能继续透传。
     */
    private val restrictedRequestHeaders = setOf(
        HttpHeaders.Host.lowercase(),
        HttpHeaders.ContentLength.lowercase(),
        HttpHeaders.TransferEncoding.lowercase(),
        HttpHeaders.Connection.lowercase(),
        "Keep-Alive".lowercase(),
        HttpHeaders.TE.lowercase(),
        HttpHeaders.Trailer.lowercase(),
        HttpHeaders.Upgrade.lowercase(),
        HttpHeaders.Expect.lowercase(),
        HttpHeaders.ProxyAuthorization.lowercase(),
        HttpHeaders.ProxyAuthenticate.lowercase(),
    )

    /**
     * 需要从上游响应中过滤掉的受限响应头。
     * Content-Type 与 Content-Length 会通过 OutgoingContent 的属性单独回写，避免重复。
     */
    private val restrictedResponseHeaders = setOf(
        HttpHeaders.ContentType.lowercase(),
        HttpHeaders.ContentLength.lowercase(),
        HttpHeaders.TransferEncoding.lowercase(),
        HttpHeaders.Connection.lowercase(),
        "Keep-Alive".lowercase(),
        HttpHeaders.TE.lowercase(),
        HttpHeaders.Trailer.lowercase(),
        HttpHeaders.Upgrade.lowercase(),
        HttpHeaders.ProxyAuthorization.lowercase(),
        HttpHeaders.ProxyAuthenticate.lowercase(),
    )

    private val logger = KotlinLogging.logger("JvmReverseProxyServer")

    @Volatile
    private var proxyEngine: EmbeddedServer<*, *>? = null

    /**
     * 启动本地反向代理服务。
     * 若服务已经启动，则直接复用现有实例，避免重复占用端口。
     */
    @Synchronized
    fun start() {
        if (proxyEngine != null) {
            return
        }

        try {
            proxyEngine = embeddedServer(
                Netty,
                environment = applicationEnvironment(),
                configure = {
                    connectionGroupSize = 1
                    workerGroupSize = proxyWorkerThreadCount
                    callGroupSize = proxyCallThreadCount
                    runningLimit = proxyRunningRequestLimit
                    connector {
                        host = PROXY_HOST
                        port = PROXY_PORT
                    }
                },
                module = {
                    installProxyRoutes()
                    installCachePlaybackRoutes()
                    // HLS 分片缓存通过独立路由处理，避免和单文件 Range 播放入口混用。
                    installHlsPlaybackRoutes()
                }
            ).start(wait = false)
            logger.info {
                "本地反向代理服务已启动：http://$PROXY_HOST:$PROXY_PORT/proxy，" +
                        "worker=$proxyWorkerThreadCount，call=$proxyCallThreadCount，" +
                        "runningLimit=$proxyRunningRequestLimit"
            }
        } catch (throwable: Throwable) {
            logger.error(throwable) { "启动本地反向代理服务失败" }
        }
    }

    /**
     * 停止本地反向代理服务。
     * HttpClient 由 DataSourceManager 统一托管，这里只关闭本地 Netty 服务。
     */
    @Synchronized
    fun stop() {
        proxyEngine?.stop(gracePeriodMillis = 1_000, timeoutMillis = 3_000)
        proxyEngine = null
    }

    /**
     * 将目标地址包装为本地代理地址。
     * 若传入地址已经是当前代理地址，则直接返回，避免重复嵌套代理。
     */
    fun wrapTargetUrl(targetUrl: String): String {
        if (targetUrl.isBlank()) {
            return targetUrl
        }

        val parsedUrl = runCatching { Url(targetUrl) }.getOrNull()
        if (parsedUrl != null && isSelfProxyUrl(parsedUrl)) {
            return targetUrl
        }

        val encodedUrl = URLEncoder.encode(targetUrl, StandardCharsets.UTF_8)
        return "http://$PROXY_HOST:$PROXY_PORT/proxy?url=$encodedUrl"
    }

    /**
     * 将缓存会话包装为本地播放地址。
     */
    fun wrapCachePlaybackUrl(sessionId: Long): String {
        return "http://$PROXY_HOST:$PROXY_PORT/cache-play?id=$sessionId"
    }

    /**
     * 将 HLS playlist 包装为本地缓存代理地址。
     *
     * @param sessionId HLS 缓存会话 id。
     * @param playlistUrl 上游 m3u8 地址。
     * @return `/hls-play` 本地代理地址。
     */
    internal fun wrapHlsPlaylistUrl(
        sessionId: Long,
        playlistUrl: String,
    ): String {
        return wrapHlsResourceUrl(
            sessionId = sessionId,
            resourceUrl = playlistUrl,
            type = HlsResourceKind.PLAYLIST.routeValue,
            cacheable = false,
        )
    }

    /**
     * 将 HLS 分片、key、map 或 child playlist 包装为本地缓存代理地址。
     *
     * @param sessionId HLS 缓存会话 id。
     * @param resourceUrl 上游真实资源地址。
     * @param type 资源类型，对应 [HlsResourceKind.routeValue]。
     * @param cacheable true 表示该资源可以被缓存层写入本地文件。
     */
    internal fun wrapHlsResourceUrl(
        sessionId: Long,
        resourceUrl: String,
        type: String,
        cacheable: Boolean,
    ): String {
        // 原始 URL 可能包含 query、# 或非 ASCII 字符，放进代理参数前必须编码。
        val encodedUrl = URLEncoder.encode(resourceUrl, StandardCharsets.UTF_8)
        // 使用短标记减少 playlist 重写后的 URL 长度。
        val cacheFlag = if (cacheable) "1" else "0"
        return "http://$PROXY_HOST:$PROXY_PORT/hls-play?id=$sessionId&type=$type&cache=$cacheFlag&url=$encodedUrl"
    }

    /**
     * 安装代理路由。
     * 这里使用通用 handle 以兼容 GET 以及后续扩展的 POST、PUT、DELETE 等方法。
     */
    private fun Application.installProxyRoutes() {
        routing {
            route("/proxy") {
                get { handleProxy(call) }
                head { handleProxy(call) }
                post { handleProxy(call) }
                put { handleProxy(call) }
                patch { handleProxy(call) }
                delete { handleProxy(call) }
                options { handleProxy(call) }
            }
        }
    }

    /**
     * 安装 JVM 播放缓存路由。
     * VLC 通过这个本地 HTTP 入口读取正在下载的缓存文件，避免直接打开增长中的 file。
     */
    private fun Application.installCachePlaybackRoutes() {
        routing {
            route("/cache-play") {
                get { handleCachePlayback(call, responseBody = true) }
                head { handleCachePlayback(call, responseBody = false) }
            }
        }
    }

    /**
     * 安装 JVM HLS 播放缓存路由。
     *
     * GET 返回实际内容，HEAD 只返回播放器探测所需的响应头。
     */
    private fun Application.installHlsPlaybackRoutes() {
        routing {
            route("/hls-play") {
                get { handleHlsPlayback(call, responseBody = true) }
                head { handleHlsPlayback(call, responseBody = false) }
            }
        }
    }

    /**
     * 执行单次代理请求。
     * 整个响应链路均采用流式转发，避免将完整视频或大文件读入内存。
     */
    private suspend fun handleProxy(call: ApplicationCall) {
        val targetUrl = validateTargetUrl(call.request.queryParameters["url"])
        if (targetUrl == null) {
            call.respondText(
                text = "url 参数无效，仅支持 http/https 且不能回指当前代理服务。",
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val httpClient = currentProxyHttpClient()
        if (httpClient == null) {
            call.respondText(
                text = "代理服务尚未拿到可用的数据源客户端。",
                status = HttpStatusCode.ServiceUnavailable
            )
            return
        }

        try {
            val upstreamRequest = HttpRequestBuilder().apply {
                url.takeFrom(targetUrl)
                method = call.request.httpMethod
                // DataSourceManager 返回的 HttpClient 默认 expectSuccess=true，
                // 代理场景需要按原样回传 4xx/5xx，因此这里对单次请求显式关闭。
                expectSuccess = false
                // JVM 播放代理是长时间流式转发，不能受普通接口 requestTimeout 总耗时限制影响。
                timeout {
                    requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                }

                // 先透传原始请求头，再补充业务要求中的自定义请求头。
                copyHeaders(
                    source = call.request.headers,
                    target = headers,
                    restrictedHeaders = restrictedRequestHeaders
                )

                // 仅在可能存在请求体的方法中透传输入流，避免额外消费无意义的空流。
                if (shouldForwardRequestBody(call.request.httpMethod)) {
                    setBody(call.receiveChannel())
                }
            }

            withContext(Dispatchers.IO) {
                httpClient.prepareRequest(upstreamRequest).execute { upstreamResponse ->
                    val responseHeaders = Headers.build {
                        copyHeaders(
                            source = upstreamResponse.headers,
                            target = this,
                            restrictedHeaders = restrictedResponseHeaders
                        )
                    }

                    call.respond(
                        object : OutgoingContent.WriteChannelContent() {
                            /**
                             * 将上游状态码原样回传给下游客户端。
                             */
                            override val status: HttpStatusCode = upstreamResponse.status

                            /**
                             * 将上游 Content-Type 原样回传，确保 m3u8、ts、mp4 等媒体类型可被正确识别。
                             */
                            override val contentType: ContentType? =
                                upstreamResponse.headers[HttpHeaders.ContentType]
                                    ?.let(ContentType::parse)

                            /**
                             * 将上游 Content-Length 回传给下游，便于播放器处理进度与分段请求。
                             */
                            override val contentLength: Long? =
                                upstreamResponse.headers[HttpHeaders.ContentLength]?.toLongOrNull()

                            /**
                             * 除了单独处理的 Content-Type 与 Content-Length，其余响应头全部透传。
                             */
                            override val headers: Headers = responseHeaders

                            /**
                             * 直接将上游字节流写回客户端，避免整包缓冲。
                             */
                            override suspend fun writeTo(channel: ByteWriteChannel) {
                                upstreamResponse.bodyAsChannel().copyTo(channel)
                            }
                        }
                    )
                }
            }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            logger.error(throwable) { "代理请求失败，targetUrl=$targetUrl" }
            call.respondText(
                text = "代理请求失败：${throwable.message ?: "unknown"}",
                status = HttpStatusCode.BadGateway
            )
            throw throwable
        }
    }

    private suspend fun handleCachePlayback(
        call: ApplicationCall,
        responseBody: Boolean,
    ) {
        val sessionId = call.request.queryParameters["id"]?.toLongOrNull()
        if (sessionId == null) {
            call.respondText(
                text = "id 参数无效。",
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val cacheController = JvmDownloadCacheController.controllerOrNull()
        if (cacheController == null) {
            call.respondText(
                text = "播放缓存服务不可用。",
                status = HttpStatusCode.ServiceUnavailable
            )
            return
        }

        val snapshot = cacheController.getSessionSnapshot(sessionId)
        if (snapshot == null) {
            call.respondText(
                text = "播放缓存会话不存在。",
                status = HttpStatusCode.NotFound
            )
            return
        }
        if (snapshot.status == CacheStatus.FAILED || snapshot.status == CacheStatus.CANCELLED) {
            call.respondText(
                text = "播放缓存会话不可用。",
                status = HttpStatusCode.ServiceUnavailable
            )
            return
        }

        // 响应头里的媒体类型和总长度以当前播放流的上游响应为准，转码流不能复用原始文件大小。
        if (!cacheController.awaitResolvedMediaInfo(sessionId)) {
            call.respondText(
                text = "播放缓存媒体信息不可用。",
                status = HttpStatusCode.BadGateway
            )
            return
        }
        val resolvedSnapshot = cacheController.getSessionSnapshot(sessionId) ?: snapshot

        // 先解析 VLC 请求的原始 Range，再根据缓存状态裁剪实际响应范围。
        val requestedRange = parseRangeHeader(
            rangeHeader = call.request.headers[HttpHeaders.Range],
            totalBytes = resolvedSnapshot.totalBytes
        )
        if (requestedRange == null) {
            call.respondText(
                text = "Range 请求无效。",
                status = HttpStatusCode.RequestedRangeNotSatisfiable
            )
            return
        }

        // 缓存未完成时不一次声明整首歌长度，避免播放器等待完整响应结束才进入播放。
        val responseRange = resolveCacheResponseRange(
            requestedRange = requestedRange,
            snapshot = resolvedSnapshot,
        )
        val contentLength = responseRange.endInclusive - responseRange.start + 1
        val responseHeaders = Headers.build {
            append(HttpHeaders.AcceptRanges, "bytes")
            append(HttpHeaders.CacheControl, "no-store")
            if (responseRange.isPartial) {
                append(
                    HttpHeaders.ContentRange,
                    "bytes ${responseRange.start}-${responseRange.endInclusive}/${resolvedSnapshot.totalBytes}"
                )
            }
        }

        call.respond(
            object : OutgoingContent.WriteChannelContent() {
                override val status: HttpStatusCode =
                    if (responseRange.isPartial) HttpStatusCode.PartialContent else HttpStatusCode.OK
                override val contentType: ContentType? =
                    resolvedSnapshot.contentType.takeIf { it.isNotBlank() }?.let { rawContentType ->
                        runCatching { ContentType.parse(rawContentType) }.getOrNull()
                    }
                override val contentLength: Long = contentLength
                override val headers: Headers = responseHeaders

                override suspend fun writeTo(channel: ByteWriteChannel) {
                    if (!responseBody) {
                        // HEAD 请求只需要响应头，不写响应体。
                        return
                    }
                    when (
                        cacheController.writeSessionTo(
                            sessionId = sessionId,
                            requestedStart = responseRange.start,
                            requestedEndInclusive = responseRange.endInclusive,
                            output = channel,
                        )
                    ) {
                        CacheStreamResult.Streamed -> Unit
                        CacheStreamResult.NotFound,
                        CacheStreamResult.Unavailable -> Unit
                    }
                }
            }
        )
    }

    /**
     * 处理 HLS 本地播放请求。
     *
     * playlist 请求会读取上游 m3u8 并重写资源地址；分片、key 和 map 请求优先命中缓存，
     * 未命中时回源并按资源缓存标记决定是否落盘。
     */
    private suspend fun handleHlsPlayback(
        call: ApplicationCall,
        responseBody: Boolean,
    ) {
        // 本地代理 URL 必须包含会话 id、资源原始 URL 和资源类型。
        val sessionId = call.request.queryParameters["id"]?.toLongOrNull()
        val resourceUrl = call.request.queryParameters["url"]
        val kind = HlsResourceKind.fromRouteValue(call.request.queryParameters["type"])
        if (sessionId == null || resourceUrl.isNullOrBlank() || kind == null) {
            call.respondText(
                text = "HLS 参数无效。",
                status = HttpStatusCode.BadRequest,
            )
            return
        }

        // HLS 缓存控制器是 JVM 平台能力，获取不到时说明当前播放缓存服务不可用。
        val cacheController = JvmDownloadCacheController.controllerOrNull()
        if (cacheController == null) {
            call.respondText(
                text = "播放缓存服务不可用。",
                status = HttpStatusCode.ServiceUnavailable,
            )
            return
        }

        if (kind == HlsResourceKind.PLAYLIST) {
            // playlist 不能直接缓存旧文本，因为其中的子资源地址需要按当前 session 重写。
            val playlist = cacheController.readHlsPlaylist(
                sessionId = sessionId,
                playlistUrl = resourceUrl,
            )
            if (playlist == null) {
                call.respondText(
                    text = "HLS playlist 不可用。",
                    status = HttpStatusCode.BadGateway,
                )
                return
            }
            // Ktor 响应体需要 ByteArray，这里只转换重写后的 playlist 文本。
            val bytes = playlist.text.encodeToByteArray()
            call.respond(
                object : OutgoingContent.WriteChannelContent() {
                    /** playlist 请求成功时固定返回 200。 */
                    override val status: HttpStatusCode = HttpStatusCode.OK
                    /** 使用上游 playlist 类型，解析失败时由 Ktor 省略 Content-Type。 */
                    override val contentType: ContentType? = parseContentTypeOrNull(playlist.contentType)
                    /** 重写后的 playlist 字节长度。 */
                    override val contentLength: Long = bytes.size.toLong()
                    /** playlist 是会话内动态重写内容，不交给外部 HTTP 缓存。 */
                    override val headers: Headers = Headers.build {
                        append(HttpHeaders.CacheControl, "no-store")
                    }

                    override suspend fun writeTo(channel: ByteWriteChannel) {
                        if (responseBody) {
                            // HEAD 请求会传入 responseBody=false，只返回响应头。
                            channel.writeFully(bytes)
                        }
                    }
                }
            )
            return
        }

        // cache=1 来自 playlist 重写阶段，表示该资源适合完整写入分片缓存。
        val cacheable = call.request.queryParameters["cache"] == "1"
        // 若资源已经缓存，先拿快照补齐 Content-Type / Content-Length 响应头。
        val snapshot = cacheController.getHlsResourceSnapshot(
            sessionId = sessionId,
            resourceUrl = resourceUrl,
        )
        // 未命中缓存时仍要给播放器一个保守的媒体类型。
        val fallbackContentType = defaultHlsResourceContentType(kind)
        call.respond(
            object : OutgoingContent.WriteChannelContent() {
                /** 分片代理成功入口固定返回 200，具体失败由写出阶段转为空响应。 */
                override val status: HttpStatusCode = HttpStatusCode.OK
                /** 优先使用缓存记录的真实类型，没有记录时按资源类型兜底。 */
                override val contentType: ContentType? =
                    parseContentTypeOrNull(snapshot?.contentType ?: fallbackContentType)
                /** 只有缓存命中且记录了长度时才声明 Content-Length。 */
                override val contentLength: Long? = snapshot?.contentLength
                /** HLS 分片由播放器按 playlist 调度，不使用外部 HTTP 缓存。 */
                override val headers: Headers = Headers.build {
                    append(HttpHeaders.CacheControl, "no-store")
                }

                override suspend fun writeTo(channel: ByteWriteChannel) {
                    if (!responseBody) {
                        // HEAD 请求只需要响应头，不读取缓存或回源。
                        return
                    }
                    when (
                        cacheController.writeHlsResourceTo(
                            sessionId = sessionId,
                            resourceUrl = resourceUrl,
                            kind = kind,
                            cacheable = cacheable,
                            output = channel,
                        )
                    ) {
                        HlsResourceStreamResult.Streamed -> Unit
                        HlsResourceStreamResult.NotFound,
                        HlsResourceStreamResult.Unavailable -> Unit
                    }
                }
            }
        )
    }

    /**
     * 校验并解析目标地址。
     * 仅允许代理 http/https 绝对地址，并阻止请求再次回到当前代理服务导致死循环。
     */
    private fun validateTargetUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) {
            return null
        }

        val parsedUrl = runCatching { Url(rawUrl) }.getOrNull() ?: return null
        if (parsedUrl.host.isBlank()) {
            return null
        }
        if (parsedUrl.protocol != URLProtocol.HTTP && parsedUrl.protocol != URLProtocol.HTTPS) {
            return null
        }
        if (isSelfProxyUrl(parsedUrl)) {
            return null
        }
        return rawUrl
    }

    /**
     * 判断目标地址是否再次指向当前代理入口。
     * 该校验可以防止 /proxy?url=http://localhost:19180/proxy?... 造成递归代理。
     */
    private fun isSelfProxyUrl(targetUrl: Url): Boolean {
        val normalizedHost = targetUrl.host.lowercase()
        val isLocalHost = normalizedHost == "localhost" ||
                normalizedHost == "127.0.0.1" ||
                normalizedHost == "::1"
        val targetPort = if (targetUrl.port > 0) targetUrl.port else targetUrl.protocol.defaultPort

        return isLocalHost &&
                targetPort == PROXY_PORT &&
                targetUrl.encodedPath in setOf("/proxy", "/cache-play", "/hls-play")
    }

    /**
     * 安全解析 Content-Type。
     */
    private fun parseContentTypeOrNull(rawContentType: String?): ContentType? {
        if (rawContentType.isNullOrBlank()) {
            return null
        }
        return runCatching {
            // 上游可能返回非标准类型，解析失败时返回 null 交给 Ktor 省略。
            ContentType.parse(rawContentType)
        }.getOrNull()
    }

    /**
     * 根据 HLS 资源类型提供兜底 Content-Type。
     */
    private fun defaultHlsResourceContentType(kind: HlsResourceKind): String {
        return when (kind) {
            // key 和 map 都按二进制处理，避免播放器按文本解析。
            HlsResourceKind.KEY,
            HlsResourceKind.MAP -> "application/octet-stream"
            // 传统 HLS 媒体分片默认是 MPEG-TS。
            HlsResourceKind.SEGMENT -> "video/mp2t"
            // playlist 分支通常不会走到这里，保留兜底值。
            HlsResourceKind.PLAYLIST -> "application/vnd.apple.mpegurl"
        }
    }

    /**
     * 从 DataSourceManager 获取当前可用的 HttpClient。
     * 这里按请求实时读取，确保切换数据源后代理自动复用新的客户端配置。
     */
    private fun currentProxyHttpClient(): HttpClient? {
        return runCatching {
            get<DataSourceManager>().getHttpClient()
        }.onFailure { throwable ->
            logger.warn(throwable) { "当前没有可用的数据源 HttpClient" }
        }.getOrNull()
    }

    /**
     * 复制头信息，同时过滤 hop-by-hop 与受限头。
     * 这里保留多值头，确保 Range、Set-Cookie、Accept 等场景不会丢值。
     */
    private fun copyHeaders(
        source: Headers,
        target: HeadersBuilder,
        restrictedHeaders: Set<String>
    ) {
        source.names().forEach { headerName ->
            if (headerName.lowercase() in restrictedHeaders) {
                return@forEach
            }
            source.getAll(headerName)?.forEach { headerValue ->
                target.append(headerName, headerValue)
            }
        }
    }

    /**
     * 判断当前请求方法是否需要转发请求体。
     * GET、HEAD、OPTIONS 通常不携带请求体，其余方法保持透传能力。
     */
    private fun shouldForwardRequestBody(method: HttpMethod): Boolean {
        return method != HttpMethod.Get &&
                method != HttpMethod.Head &&
                method != HttpMethod.Options
    }

    /**
     * 计算缓存播放实际响应给播放器的 Range。
     * 缓存完成后按播放器请求原样返回；缓存未完成时裁剪成小窗口，促使播放器连续发起后续 Range。
     *
     * @param requestedRange 播放器原始请求的字节范围。
     * @param snapshot 当前缓存会话的只读快照。
     * @return 本地代理实际声明并写出的响应字节范围。
     */
    private fun resolveCacheResponseRange(
        requestedRange: PlaybackRange,
        snapshot: cn.xybbz.music.CacheSessionSnapshot,
    ): PlaybackRange {
        if (snapshot.status == CacheStatus.COMPLETED) {
            return requestedRange
        }

        // 未完成缓存只暴露一个可快速返回的小窗口，避免单个响应绑定到整首歌。
        val windowEnd = (requestedRange.start + CACHE_STREAM_WINDOW_BYTES - 1)
            .coerceAtMost(snapshot.totalBytes - 1)
        val endInclusive = minOf(requestedRange.endInclusive, windowEnd)

        return requestedRange.copy(
            endInclusive = endInclusive,
            isPartial = true,
        )
    }

    private fun parseRangeHeader(
        rangeHeader: String?,
        totalBytes: Long,
    ): PlaybackRange? {
        if (totalBytes <= 0L) {
            return null
        }
        if (rangeHeader.isNullOrBlank()) {
            return PlaybackRange(
                start = 0L,
                endInclusive = totalBytes - 1,
                isPartial = false,
            )
        }

        if (!rangeHeader.startsWith("bytes=")) {
            return null
        }

        val rangeValue = rangeHeader.removePrefix("bytes=").substringBefore(',')
        val startText = rangeValue.substringBefore('-', "")
        val endText = rangeValue.substringAfter('-', "")

        val start = if (startText.isBlank()) {
            val suffixLength = endText.toLongOrNull() ?: return null
            (totalBytes - suffixLength).coerceAtLeast(0L)
        } else {
            startText.toLongOrNull() ?: return null
        }
        val endInclusive = if (endText.isBlank() || startText.isBlank()) {
            totalBytes - 1
        } else {
            endText.toLongOrNull() ?: return null
        }.coerceAtMost(totalBytes - 1)

        if (start < 0L || start > endInclusive || start >= totalBytes) {
            return null
        }

        return PlaybackRange(
            start = start,
            endInclusive = endInclusive,
            isPartial = true,
        )
    }

    private data class PlaybackRange(
        val start: Long,
        val endInclusive: Long,
        val isPartial: Boolean,
    )
}
