package cn.xybbz.download.core

import io.ktor.client.HttpClient

interface HttpClientFactory {
    fun createHttpClient(): HttpClient
}