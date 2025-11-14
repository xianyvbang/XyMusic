package cn.xybbz.api.base

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Streaming
import retrofit2.http.Url

interface IDownLoadApi {

    @GET
    @Streaming
    fun downloadFile(
        @Url fileUrl: String,
        @HeaderMap headers: Map<String, String>,
        @Header("Range") range: String /*= "bytes=0-"*/,
    ): Call<ResponseBody>
}