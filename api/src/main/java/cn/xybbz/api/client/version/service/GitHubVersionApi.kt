package cn.xybbz.api.client.version.service

import cn.xybbz.api.client.version.data.ReleasesData
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.constants.ApiConstants.HEADER_DOWNLOAD
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

interface GitHubVersionApi {

    @GET("/repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestReleasesInfo(
        @Path(value = "owner") owner: String = "xianyvbang",
        @Path(value = "repo") repo: String = "XyMusic"
    ): ReleasesData?

    @GET
    @Streaming
    suspend fun downloadFile(
        @Header(HEADER_DOWNLOAD) download: Boolean = true,
        @Header(ApiConstants.ACCEPT) accept: String = "application/octet-stream",
        @Header("User-Agent") agent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
        @Url fileUrl: String,
        @Header("Range") range: String /*= "bytes=0-"*/,
        @Header("X-Already-Downloaded") alreadyDownloaded: Long,
        @Header("X-Total-Bytes") totalBytes: Long
    ): Response<ResponseBody>
}