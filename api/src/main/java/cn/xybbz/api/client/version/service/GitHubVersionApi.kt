package cn.xybbz.api.client.version.service

import cn.xybbz.api.base.IDownLoadApi
import cn.xybbz.api.client.version.data.ReleasesData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

interface GitHubVersionApi : IDownLoadApi {

    @GET("/repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestReleasesInfo(
        @Path(value = "owner") owner: String = "xianyvbang",
        @Path(value = "repo") repo: String = "XyMusic"
    ): ReleasesData?

    @GET
    @Streaming
    override fun downloadFile(
        @Url fileUrl: String,
        @HeaderMap headers: Map<String, String> ,
        @Header("Range") range: String /*= "bytes=0-"*/,
    ): Call<ResponseBody>
}