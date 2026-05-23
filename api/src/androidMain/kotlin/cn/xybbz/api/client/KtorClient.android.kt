package cn.xybbz.api.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import java.util.concurrent.TimeUnit

actual fun provideClient(): HttpClient {
    return provideClient(proxy = null)
}

actual fun provideClient(proxy: ProxyConfig?): HttpClient {
    return HttpClient(OkHttp) {
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
