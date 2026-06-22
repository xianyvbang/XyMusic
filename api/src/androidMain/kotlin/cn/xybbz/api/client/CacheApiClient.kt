package cn.xybbz.api.client

import cn.xybbz.api.TokenServer
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.okhttp.proxy.OkhttpProxyManager
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class CacheApiClient{

    var okhttpClientFunction: (() -> OkHttpClient) = { getOkHttpClient() }


    private fun getOkHttpClient():OkHttpClient {
        return OkHttpClient.Builder()
            .proxySelector(OkhttpProxyManager.proxySelector())
            .addInterceptor{chain ->
                val request = chain.request()
                // 每次 OkHttp 请求只读取一次认证快照，避免同一请求混用不同版本的认证参数。
                val authenticatedRequestData = TokenServer.authenticatedRequestData
                val requestBuilder = request.newBuilder()
                    .addHeader(
                        authenticatedRequestData.tokenHeaderName,
                        authenticatedRequestData.token
                    )

                if (authenticatedRequestData.headerMap.isNotEmpty()) {
                    authenticatedRequestData.headerMap.forEach { (key, value) ->
                        requestBuilder.addHeader(key, value)
                    }
                }
                var originalUrl = request.url

                if (authenticatedRequestData.queryMap.isNotEmpty()) {
                    // 拼接默认参数
                    val newUrlBuilder = originalUrl.newBuilder()
                    authenticatedRequestData.queryMap.forEach { (key, value) ->
                        newUrlBuilder.addQueryParameter(key, value)
                    }
                    originalUrl = newUrlBuilder.build()
                }
                val newRequest = requestBuilder
                    .url(originalUrl)
                    .build()
                chain.proceed(newRequest)
            }

            .connectTimeout(ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .readTimeout(ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .writeTimeout(ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true).build()
    }

}
