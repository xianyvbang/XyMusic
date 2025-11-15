package cn.xybbz.api.okhttp.subsonic

import android.util.Log
import cn.xybbz.api.client.subsonic.data.SubsonicParentResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.enums.subsonic.Status
import cn.xybbz.api.exception.ServiceException
import cn.xybbz.api.exception.UnauthorizedException
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.Interceptor
import okhttp3.Response

//todo 这里 if (header != "application/octet-stream" && header?.contains("audio") == false && ifSubsonic())的判断需要考虑使用其他方式
class SubsonicNetworkStatusInterceptor(private val ifSubsonic: () -> Boolean) : Interceptor {

    lateinit var adapter: JsonAdapter<SubsonicResponse<SubsonicParentResponse>>

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        val originalResponse = chain.proceed(request)
        // 对成功响应先peek出来检查
        val header = originalResponse.header("content-type")
        if (header != ApiConstants.DOWNLOAD_ACCEPT && header?.contains("audio") == false && ifSubsonic()) {
            getJsonAdapter()
            val peekedBody = originalResponse.peekBody(Long.MAX_VALUE).string()

            if (peekedBody.startsWith("{")) {
                if (peekedBody.isNotBlank()) {
                    val result = try {
                        adapter.fromJson(peekedBody)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }

                    if (result?.subsonicResponse?.status == Status.Failed) {
                        Log.e("error","接口报错响应:${result.subsonicResponse}")
                        val error = result.subsonicResponse.error
                        if (error?.code == 40) {
                            throw UnauthorizedException(
                                msg = "请求接口---> ${request.url.encodedPath} ${error.message}",
                                statusCode = originalResponse.code,
                                responsePhrase = peekedBody
                            )
                        } else {
                            throw ServiceException(
                                message = error?.message ?: "",
                                code = error?.code ?: 500
                            )
                        }
                    }
                }
            }
        }
        return originalResponse
    }

    private fun getJsonAdapter() {
        if (!this::adapter.isInitialized) {
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(
                SubsonicResponse::class.java,
                SubsonicParentResponse::class.java
            )
            adapter = moshi.adapter(type)
        }
    }
}