package cn.xybbz.api.okhttp

import android.util.Log
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.exception.ServiceException
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer

class NetWorkInterceptor(
    private val tokenHeaderName: () -> String,
    private val token: () -> String?,
    private val queryMap: () -> Map<String, String>?,
    private val headerMap: () -> Map<String, String>?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()
        try {
            val token = token()
            val requestNewBuilder = request.newBuilder()
            if (token != null) {
                requestNewBuilder
                    .addHeader(tokenHeaderName(), token)
                    .addHeader(
                        name = ApiConstants.ACCEPT,
                        value = ApiConstants.HEADER_ACCEPT,
                    )

            }

            val headerMap = headerMap()
            if (!headerMap.isNullOrEmpty()) {
                headerMap.forEach { key, value ->
                    requestNewBuilder.addHeader(key, value)
                }
            }

            request =
                requestNewBuilder.build()
            val queryMap = queryMap()
            if (!queryMap.isNullOrEmpty()) {
                val originalUrl = request.url
                // 拼接默认参数
                val newUrlBuilder = originalUrl.newBuilder()
                queryMap.forEach { key, value ->
                    newUrlBuilder.addQueryParameter(key, value)
                }
                val newUrl = newUrlBuilder.build()


                request = request.newBuilder()
                    .url(newUrl)
                    .build()
            }


            return chain.proceed(request)
        } catch (e: Exception) {
            Log.e("api", "请求接口报错: ${request.url.encodedPath}",e)
            throw e
        }

    }
}


class LoggingInterceptor : Interceptor {
    @Throws(Exception::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestBody = request.body
        Log.i("api", "发起请求 ${request.method} ${request.url}")
        requestBody?.let {
            Log.i("api", "请求体: ${bodyToString(it)}")
        }
        val originalResponse = chain.proceed(request)
        if (!originalResponse.isSuccessful && originalResponse.code !in 301..304) {
            if (originalResponse.code != ApiConstants.UNAUTHORIZED) {
                /*throw UnauthorizedException(
                    msg = "请求接口---> ${request.url.encodedPath} ${originalResponse.message}",
                    statusCode = originalResponse.code,
                    responsePhrase = "登陆失败"
                )*/
                throw ServiceException(
                    message = originalResponse.message,
                    code = originalResponse.code
                )
            } else {

            }
        }
        return originalResponse
    }


    private fun bodyToString(request: RequestBody): String {
        val buffer = Buffer()
        request.writeTo(buffer)
        return buffer.readUtf8()
    }

}


