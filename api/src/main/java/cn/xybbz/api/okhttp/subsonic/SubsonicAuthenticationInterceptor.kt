package cn.xybbz.api.okhttp.subsonic

import android.util.Log
import cn.xybbz.api.enums.subsonic.ResponseFormatType
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Subsonic验证参数拼接拦截器
 * @author 刘梦龙
 * @date 2025/06/11
 * @constructor 创建[SubsonicAuthenticationInterceptor]
 * @param [username] 用户名
 * @param [passwordMd5] md5(密码+盐)计算出的身份验证令牌
 * @param [encryptedSalt] 密码加密盐(随机生成,最少6位)
 * @param [protocolVersion] 客户端实现的协议版本
 * @param [clientName] 客户端名称
 * @param [responseFormat] 返回格式，可选值: xml, json, jsonp
 */
class SubsonicAuthenticationInterceptor(
    val username: String,
    val passwordMd5: String,
    val encryptedSalt: String,
    val protocolVersion: String,
    val clientName: String,
    val responseFormat: String = ResponseFormatType.JSON.serialName
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        val emptyMap = emptyMap<Int, String>()
        emptyMap.forEach { index,key -> }
        Log.i("api","开始拼接校验数据")
        // 拼接默认参数
        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("u", username)
            .addQueryParameter("t", passwordMd5)
            .addQueryParameter("s", encryptedSalt)
            .addQueryParameter("v", protocolVersion)
            .addQueryParameter("c", clientName)
            .addQueryParameter("f", responseFormat)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}