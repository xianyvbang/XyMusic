package cn.xybbz.api.client

import cn.xybbz.api.constants.ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS
import cn.xybbz.api.converter.jsonSerializer
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json

expect fun provideClient(): HttpClient

expect fun provideClient(proxy: ProxyConfig?): HttpClient

internal fun HttpClientConfig<*>.installXyCommonClientConfig() {
    install(Logging) {
        logger = object : Logger {
            private val logger = KotlinLogging.logger {}
            override fun log(message: String) {
                logger.info { message }
            }
        }
        level = LogLevel.HEADERS
    }
    install(ContentNegotiation) {
        json(jsonSerializer)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = DEFAULT_TIMEOUT_MILLISECONDS
        connectTimeoutMillis = DEFAULT_TIMEOUT_MILLISECONDS
        socketTimeoutMillis = DEFAULT_TIMEOUT_MILLISECONDS
    }
}
