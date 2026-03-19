package cn.xybbz.api.base

import cn.xybbz.api.constants.ApiConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * 下载方法的基础API
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[IDownLoadApi]
 */
interface IDownLoadApi {

    @GET
    @Streaming
    fun downloadFile(
        @Url fileUrl: String,
        @HeaderMap headers: Map<String, String> = mapOf(
            ApiConstants.ACCEPT to ApiConstants.DOWNLOAD_ACCEPT
        ),
        @Header("Range") range: String, /*= "bytes=0-"*/
    ): Call<ResponseBody>
}