package cn.xybbz.api.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5

actual fun provideClient(): HttpClient {
    return HttpClient(Apache5)
}