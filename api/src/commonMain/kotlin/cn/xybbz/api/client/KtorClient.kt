package cn.xybbz.api.client

import cn.xybbz.api.constants.ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS
import cn.xybbz.api.converter.ValidatingJsonConverter
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType

expect fun provideClient(): HttpClient

expect fun provideClient(proxy: ProxyConfig?): HttpClient

internal fun HttpClientConfig<*>.installXyCommonClientConfig() {
    install(Logging) {
        logger = object : Logger {
            private val logger = KotlinLogging.logger {}
            override fun log(message: String) {
                logger.info { HttpLogSanitizer.sanitize(message) }
            }
        }
        level = if (KtorClient.debug) LogLevel.ALL else LogLevel.NONE
    }
    install(ContentNegotiation) {
        // 使用带业务状态校验的 JSON 转换器，Subsonic 响应反序列化后立即校验 status
        register(ContentType.Application.Json, ValidatingJsonConverter())
    }
    install(HttpTimeout) {
        requestTimeoutMillis = DEFAULT_TIMEOUT_MILLISECONDS
        connectTimeoutMillis = DEFAULT_TIMEOUT_MILLISECONDS
        socketTimeoutMillis = DEFAULT_TIMEOUT_MILLISECONDS
    }
}
