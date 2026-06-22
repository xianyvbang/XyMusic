package cn.xybbz.api.client

import cn.xybbz.api.AuthenticatedRequestData
import cn.xybbz.api.TokenServer
import cn.xybbz.api.utils.appendCustomRequestHeaders
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.util.appendAll

/**
 * 统一配置项目内需要登录态的默认请求参数。
 * 这里用 provider 延迟读取 token/query/header，避免提前创建的 HttpClient 固化旧登录态。
 */
fun HttpClientConfig<*>.installXyAuthenticatedRequest(
    authenticatedRequestDataProvider: () -> AuthenticatedRequestData
) {
    install(XyAuthenticatedRequestPlugin) {
        this.authenticatedRequestDataProvider = authenticatedRequestDataProvider
    }
}

/**
 * 使用全局 TokenServer 配置登录态请求参数。
 * 适合 Sketch 这类会在数据源登录前创建、但请求时要读取最新登录信息的客户端。
 */
fun HttpClientConfig<*>.installTokenServerAuthenticatedRequest() {
    installXyAuthenticatedRequest(
        authenticatedRequestDataProvider = { TokenServer.authenticatedRequestData }
    )
}

private class XyAuthenticatedRequestConfig {
    /** 当前请求使用的认证参数快照。 */
    var authenticatedRequestDataProvider: () -> AuthenticatedRequestData =
        { TokenServer.authenticatedRequestData }
}

private val XyAuthenticatedRequestPlugin = createClientPlugin(
    name = "XyAuthenticatedRequest",
    createConfiguration = ::XyAuthenticatedRequestConfig
) {
    val authenticatedRequestDataProvider = pluginConfig.authenticatedRequestDataProvider

    onRequest { request, _ ->
        // 每次请求只读取一次认证快照，避免同一请求混用不同版本的 token/query/header。
        val authenticatedRequestData = authenticatedRequestDataProvider()
        request.headers.appendCustomRequestHeaders(
            sourceHeaders = request.headers.build(),
            token = authenticatedRequestData.token,
            tokenHeaderName = authenticatedRequestData.tokenHeaderName
        )
        request.headers.appendAll(authenticatedRequestData.headerMap)
        request.url.parameters.appendAll(authenticatedRequestData.queryMap)
    }
}
