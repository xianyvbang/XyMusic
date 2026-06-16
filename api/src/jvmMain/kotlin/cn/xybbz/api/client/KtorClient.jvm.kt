package cn.xybbz.api.client

import cn.xybbz.api.okhttp.proxy.ProxyManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import java.util.concurrent.TimeUnit

actual fun provideClient(): HttpClient {
    return provideClient(proxy = ProxyManager.proxySelector())
}

actual fun provideClient(proxy: ProxyConfig?): HttpClient {
    return HttpClient(OkHttp) {
        installXyCommonClientConfig()
        engine {
            this.proxy = proxy
            config {
                dispatcher(
                    Dispatcher().apply {
                        maxRequests = 128
                        maxRequestsPerHost = 32
                    }
                )
                connectionPool(
                    ConnectionPool(
                        maxIdleConnections = 32,
                        keepAliveDuration = 5,
                        timeUnit = TimeUnit.MINUTES
                    )
                )
            }
        }
    }
}

actual fun isNetworkDebugLoggingEnabled(): Boolean {
    return System.getProperty("cn.xybbz.http.debugLog") == "true"
}
