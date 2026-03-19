package cn.xybbz.api.client

import cn.xybbz.api.TokenServer
import cn.xybbz.api.constants.ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS
import cn.xybbz.api.okhttp.ImageLoggingInterceptor
import cn.xybbz.api.okhttp.LoggingInterceptor
import cn.xybbz.api.okhttp.NetWorkInterceptor
import cn.xybbz.api.okhttp.plex.PlexQueryInterceptor
import cn.xybbz.api.okhttp.proxy.ProxyManager
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class ImageApiClient : DefaultApiClient() {


    var okhttpClientFunction: (() -> OkHttpClient) = { apiOkHttpClient }

    /**
     * 清空数据
     */
    override fun release() {

    }
    init {
        getImageOkHttpClient()
    }

    fun getImageOkHttpClient(): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .proxySelector(ProxyManager.proxySelector())
            .addInterceptor(
                NetWorkInterceptor(
                    { TokenServer.tokenHeaderName },
                    { TokenServer.token },
                    { TokenServer.queryMap },
                    { TokenServer.headerMap }
                )
            )
            .connectTimeout(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .readTimeout(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .writeTimeout(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .addNetworkInterceptor(ImageLoggingInterceptor())
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
        apiOkHttpClient = okHttpClientBuilder
            // 可以添加其他配置，比如连接超时、读写超时等
            .build()
        return apiOkHttpClient
    }
}
