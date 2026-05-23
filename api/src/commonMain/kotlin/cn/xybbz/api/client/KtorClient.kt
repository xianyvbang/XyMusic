package cn.xybbz.api.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyConfig

expect fun provideClient(): HttpClient

expect fun provideClient(proxy: ProxyConfig?): HttpClient
