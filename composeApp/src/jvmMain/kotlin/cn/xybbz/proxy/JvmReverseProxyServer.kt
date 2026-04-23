package cn.xybbz.proxy

import cn.xybbz.api.client.DataSourceManager
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
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
import kotlinx.coroutines.CancellationException
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
            proxyEngine = embeddedServer(Netty, host = PROXY_HOST, port = PROXY_PORT) {
                installProxyRoutes()
            }.start(wait = false)
            logger.info { "本地反向代理服务已启动：http://$PROXY_HOST:$PROXY_PORT/proxy" }
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

        return isLocalHost && targetPort == PROXY_PORT && targetUrl.encodedPath == "/proxy"
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
}
