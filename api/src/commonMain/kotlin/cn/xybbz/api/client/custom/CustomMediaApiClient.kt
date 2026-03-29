package cn.xybbz.api.client.custom

import cn.xybbz.api.client.ApiFactory
import cn.xybbz.api.client.custom.data.CustomCoverQuery
import cn.xybbz.api.client.custom.data.CustomLyricsQuery
import cn.xybbz.api.client.custom.data.CustomLyricsRequestData
import cn.xybbz.api.client.provideClient
import cn.xybbz.api.constants.ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS
import cn.xybbz.api.converter.jsonSerializer
import cn.xybbz.api.okhttp.proxy.ProxyManager
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.URLBuilder
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.appendAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * 自定义媒体 API 客户端
 * 作用：
 * 1. 提供自定义歌词接口调用
 * 2. 提供自定义封面接口调用
 */
class CustomMediaApiClient : ApiFactory {

    /**
     * 自定义接口请求客户端
     * 说明：只使用 OkHttp 发起请求，复用全局代理配置。
     */
    private lateinit var httpClient: HttpClient

    private val logger = KotlinLogging.logger {}

    /**
     * 从自定义歌词 API 获取歌词文本
     * 策略：只使用单曲接口获取歌词文本。
     */
    suspend fun getLyricsText(query: CustomLyricsQuery): String? = withContext(Dispatchers.IO) {
        if (query.singleApi.trim().isBlank() || query.title.trim().isBlank()) {
            return@withContext null
        }
        val requestData = CustomLyricsRequestData.from(apiUrl = query.singleApi, query = query)
        requestCustomApiText(requestData, "请求自定义歌词接口失败")
    }

    /**
     * 从自定义封面 API 获取封面地址
     * 说明：兼容文本直返地址和 JSON 结构返回。
     */
    fun getCoverUrl(query: CustomCoverQuery): String? {
        if (query.coverApi.trim().isBlank() || listOf(
                query.title,
                query.artist,
                query.album,
                query.path
            ).all { it.isNullOrBlank() }
        ) {
            return null
        }
        val requestData = CustomLyricsRequestData.from(apiUrl = query.coverApi, query = query)
        return parseCoverUrl(requestData)
    }

    /**
     * 发起自定义接口请求
     * 说明：请求字段统一从 data class 中读取，避免硬编码字段名。
     */
    private suspend fun requestCustomApiText(
        requestData: CustomLyricsRequestData,
        errorMessage: String
    ): String? {
        if (requestData.apiUrl.isBlank()) {
            return null
        }

        return try {
            val response = httpClient.get(requestData.apiUrl) {
                headers {
                    appendAll(requestData.headers.associate { it.fieldName to it.fieldValue })
                }
                parameters {
                    appendAll(requestData.queryParams.associate { it.fieldName to it.fieldValue })
                }
            }
            val lrc: String = response.body()
            return lrc.trim().takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            logger.error(e) { "$errorMessage: ${requestData.apiUrl}" }
            null
        }
    }

    /**
     * 解析封面接口返回
     * 兼容：
     * 直接返回 URL 文本
     */
    private fun parseCoverUrl(requestData: CustomLyricsRequestData): String {
        val urlBuilder = URLBuilder(requestData.apiUrl)
        urlBuilder.let {
            it.parameters.appendAll(requestData.queryParams.associate { it.fieldName to it.fieldValue })
        }
        return urlBuilder.buildString()
    }

    /**
     * 初始化HttpClient
     */
    override fun createHttpClient(baseUrl: String, ifTmp: Boolean) {
        httpClient = provideClient().config {
            engine {
                proxy = ProxyManager.proxySelector()
            }
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
            install(HttpRequestRetry) {
                maxRetries = 2
            }
            install(HttpTimeout) {
                requestTimeoutMillis = DEFAULT_TIMEOUT_MILLISECONDS
                connectTimeoutMillis = DEFAULT_TIMEOUT_MILLISECONDS
                socketTimeoutMillis = DEFAULT_TIMEOUT_MILLISECONDS
            }
        }
    }

    /**
     * 清空数据
     */
    override fun release() {
        httpClient.close()
    }
}
