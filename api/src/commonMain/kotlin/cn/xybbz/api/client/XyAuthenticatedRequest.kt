package cn.xybbz.api.client

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
    tokenProvider: () -> String,
    tokenHeaderNameProvider: () -> String,
    queryMapProvider: () -> Map<String, String>,
    headerMapProvider: () -> Map<String, String>
) {
    install(XyAuthenticatedRequestPlugin) {
        this.tokenProvider = tokenProvider
        this.tokenHeaderNameProvider = tokenHeaderNameProvider
        this.queryMapProvider = queryMapProvider
        this.headerMapProvider = headerMapProvider
    }
}

/**
 * 使用全局 TokenServer 配置登录态请求参数。
 * 适合 Sketch 这类会在数据源登录前创建、但请求时要读取最新登录信息的客户端。
 */
fun HttpClientConfig<*>.installTokenServerAuthenticatedRequest() {
    installXyAuthenticatedRequest(
        tokenProvider = { TokenServer.token },
        tokenHeaderNameProvider = { TokenServer.tokenHeaderName },
        queryMapProvider = { TokenServer.queryMap },
        headerMapProvider = { TokenServer.headerMap }
    )
}

private class XyAuthenticatedRequestConfig {
    /** 当前请求使用的 token。 */
    var tokenProvider: () -> String = { "" }

    /** 当前请求使用的 token header 名称。 */
    var tokenHeaderNameProvider: () -> String = { TokenServer.tokenHeaderName }

    /** 当前请求追加的公共 query 参数。 */
    var queryMapProvider: () -> Map<String, String> = { emptyMap() }

    /** 当前请求追加的公共 header 参数。 */
    var headerMapProvider: () -> Map<String, String> = { emptyMap() }
}

private val XyAuthenticatedRequestPlugin = createClientPlugin(
    name = "XyAuthenticatedRequest",
    createConfiguration = ::XyAuthenticatedRequestConfig
) {
    val tokenProvider = pluginConfig.tokenProvider
    val tokenHeaderNameProvider = pluginConfig.tokenHeaderNameProvider
    val queryMapProvider = pluginConfig.queryMapProvider
    val headerMapProvider = pluginConfig.headerMapProvider

    onRequest { request, _ ->
        // 每次请求发出前再读取登录态，保证延迟登录后图片和接口请求能带上最新认证信息。
        request.headers.appendCustomRequestHeaders(
            sourceHeaders = request.headers.build(),
            token = tokenProvider(),
            tokenHeaderName = tokenHeaderNameProvider()
        )
        request.headers.appendAll(headerMapProvider())
        request.url.parameters.appendAll(queryMapProvider())
    }
}
