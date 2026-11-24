package cn.xybbz.di

import cn.xybbz.api.client.installTokenServerAuthenticatedRequest
import cn.xybbz.api.client.provideClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRedirect

/**
 * 创建 Sketch 专用 HttpClient。
 * Sketch 会在启动页/首页图片首次出现时很早创建，不能在构造期依赖延后初始化的 DataSourceManager。
 * 请求发出时再读取 TokenServer 中的最新 token/query/header，保证登录后封面请求仍带认证信息。
 * 注意：封面 URL 本身不再拼接认证参数（避免缓存 key 随登录变化），Subsonic/Navidrome 的封面认证
 * 完全依赖这里安装的认证插件补充，后续调整图片加载链路时不要绕开这个 HttpClient。
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
