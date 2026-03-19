package cn.xybbz.api.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun provideClient(): HttpClient {
    return HttpClient(OkHttp)
}