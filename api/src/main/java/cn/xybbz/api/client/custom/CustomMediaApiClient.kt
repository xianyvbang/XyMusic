package cn.xybbz.api.client.custom

import android.util.Log
import cn.xybbz.api.client.custom.data.CustomCoverQuery
import cn.xybbz.api.client.custom.data.CustomLyricsQuery
import cn.xybbz.api.client.custom.data.CustomLyricsRequestData
import cn.xybbz.api.constants.ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS
import cn.xybbz.api.okhttp.proxy.ProxyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 自定义媒体 API 客户端
 * 作用：
 * 1. 提供自定义歌词接口调用
 * 2. 提供自定义封面接口调用
 */
class CustomMediaApiClient {

    /**
     * 自定义接口请求客户端
     * 说明：只使用 OkHttp 发起请求，复用全局代理配置。
     */
    private val apiClient: OkHttpClient by lazy {
        getOkHttpClient()
    }

    /**
     * 从自定义歌词 API 获取歌词文本
     * 策略：只使用单曲接口获取歌词文本。
     */
    suspend fun getLyricsText(query: CustomLyricsQuery): String? = withContext(Dispatchers.IO) {
        try {
            if (query.singleApi.trim().isBlank() || query.title.trim().isBlank()) {
                return@withContext null
            }
            val requestData = CustomLyricsRequestData.from(apiUrl = query.singleApi, query = query)
            requestCustomApiText(requestData, "请求自定义歌词接口失败")
        } catch (e: Exception) {
            Log.e(TAG, "自定义歌词接口调用失败", e)
            null
        }
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
            ).all { it.isNullOrBlank() }) {
            return null
        }
        val requestData = CustomLyricsRequestData.from(apiUrl = query.coverApi, query = query)
        return parseCoverUrl(requestData)
    }
    /**
     * 发起自定义接口请求
     * 说明：请求字段统一从 data class 中读取，避免硬编码字段名。
     */
    private fun requestCustomApiText(
        requestData: CustomLyricsRequestData,
        errorMessage: String
    ): String? {
        if (requestData.apiUrl.isBlank()) {
            return null
        }

        val httpUrl = requestData.apiUrl.toHttpUrlOrNull()?.newBuilder()?.apply {
            requestData.queryParams.forEach { queryParam ->
                addQueryParameter(queryParam.fieldName, queryParam.fieldValue)
            }
        }?.build() ?: return null

        val requestBuilder = Request.Builder()
            .url(httpUrl)
            .get()

        requestData.headers.forEach { header ->
            requestBuilder.addHeader(header.fieldName, header.fieldValue)
        }

        return try {
            apiClient.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    return@use null
                }
                val takeIf = response.body.string().trim().takeIf { it.isNotBlank() }
                /*if (!takeIf.isNullOrEmpty())
                    json.decodeFromString<List<CustomLyricsResponseData>>(takeIf)[0].lyrics
                else null*/
                return takeIf
            }
        } catch (e: Exception) {
            Log.e(TAG, "$errorMessage: ${requestData.apiUrl}", e)
            null
        }
    }

    /**
     * 解析封面接口返回
     * 兼容：
     * 1. 直接返回 URL 文本
     * 2. JSON 对象
     * 3. JSON 数组
     */
    private fun parseCoverUrl(requestData: CustomLyricsRequestData): String? {
       return requestData.apiUrl.toHttpUrlOrNull()?.newBuilder()?.apply {
           requestData.queryParams.forEach { queryParam ->
               addQueryParameter(queryParam.fieldName, queryParam.fieldValue)
           }
       }?.build().toString()
    }

    /**
     * 获得okhttp客户端
     */
    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .proxySelector(ProxyManager.proxySelector())
            .connectTimeout(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .readTimeout(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .writeTimeout(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()
    }

    companion object {
        private const val TAG = "CustomMediaApiClient"
    }
}
