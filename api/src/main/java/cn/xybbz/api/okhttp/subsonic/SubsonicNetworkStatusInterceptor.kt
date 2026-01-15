/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

//package cn.xybbz.api.okhttp.subsonic
//
//import android.util.Log
//import cn.xybbz.api.client.subsonic.data.SubsonicParentResponse
//import cn.xybbz.api.client.subsonic.data.SubsonicResponse
//import cn.xybbz.api.constants.ApiConstants
//import cn.xybbz.api.enums.subsonic.Status
//import cn.xybbz.api.exception.ServiceException
//import cn.xybbz.api.exception.UnauthorizedException
//import com.squareup.moshi.JsonAdapter
//import com.squareup.moshi.Moshi
//import com.squareup.moshi.Types
//import okhttp3.Interceptor
//import okhttp3.Response
//
//class SubsonicNetworkStatusInterceptor() : Interceptor {
//
//    lateinit var adapter: JsonAdapter<SubsonicResponse<SubsonicParentResponse>>
//
//    override fun intercept(chain: Interceptor.Chain): Response {
//
//        val request = chain.request()
//
//        val originalResponse = chain.proceed(request)
//
//        val contentType = originalResponse.body.contentType()?.toString() ?: ""
//        val contentLength = originalResponse.body.contentLength()
//
//        val isStream = contentType.startsWith("audio/")
//                || contentType.startsWith("video/")
//                || contentType.startsWith("image/")
//                || contentType == ApiConstants.DOWNLOAD_ACCEPT
//                || contentLength > 1024 * 1024
//
//        if (!isStream) {
//            getJsonAdapter()
//            val source = originalResponse.body.source()
//            source.request(1024) // 读取前 1KB
//            val buffer = source.buffer
//            val peek = buffer.clone().readUtf8() // clone 不消费原 buffer
//            if (peek.isNotBlank()) {
//                val result = try {
//                    adapter.fromJson(peek)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    null
//                }
//
//                if (result?.subsonicResponse?.status == Status.Failed) {
//                    Log.e("error","接口报错响应:${result.subsonicResponse}")
//                    val error = result.subsonicResponse.error
//                    if (error?.code == 40) {
//                        throw UnauthorizedException(
//                            msg = "请求接口---> ${request.url.encodedPath} ${error.message}",
//                            statusCode = originalResponse.code,
//                            responsePhrase = peek
//                        )
//                    } else {
//                        throw ServiceException(
//                            message = error?.message ?: "",
//                            code = error?.code ?: 500
//                        )
//                    }
//                }
//            }
//        }
//        return originalResponse
//    }
//
//    private fun getJsonAdapter() {
//        if (!this::adapter.isInitialized) {
//            val moshi = Moshi.Builder().build()
//            val type = Types.newParameterizedType(
//                SubsonicResponse::class.java,
//                SubsonicParentResponse::class.java
//            )
//            adapter = moshi.adapter(type)
//        }
//    }
//}