package cn.xybbz.di

import cn.xybbz.api.client.installTokenServerAuthenticatedRequest
import cn.xybbz.api.client.provideClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRedirect

/**
 * 创建 Sketch 专用 HttpClient。
 * Sketch 会在启动页/首页图片首次出现时很早创建，不能在构造期依赖延后初始化的 DataSourceManager。
 * 请求发出时再读取 TokenServer 中的最新 token/query/header，保证登录后封面请求仍带认证信息。
 */
internal fun createSketchHttpClient(): HttpClient {
    return provideClient().config {
        installTokenServerAuthenticatedRequest()
        install(HttpRedirect) {
            // 部分封面接口会从 HTTPS 跳转到 HTTP 图片地址，这里允许降级跳转。
            allowHttpsDowngrade = true
        }
    }
}
