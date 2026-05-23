package cn.xybbz.api.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.darwin.Darwin

actual fun provideClient(): HttpClient {
    return provideClient(proxy = null)
}

actual fun provideClient(proxy: ProxyConfig?): HttpClient {
    return HttpClient(Darwin)
}
