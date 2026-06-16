package cn.xybbz.api.client

import cn.xybbz.api.okhttp.proxy.ProxyManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.darwin.Darwin

actual fun provideClient(): HttpClient {
    return provideClient(proxy = ProxyManager.proxySelector())
}

actual fun provideClient(proxy: ProxyConfig?): HttpClient {
    return HttpClient(Darwin) {
        installXyCommonClientConfig()
        engine {
            this.proxy = proxy
        }
    }
}

actual fun isNetworkDebugLoggingEnabled(): Boolean = false
