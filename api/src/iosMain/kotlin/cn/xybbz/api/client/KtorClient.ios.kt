package cn.xybbz.api.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun provideClient(): HttpClient {
    return HttpClient(Darwin)
}