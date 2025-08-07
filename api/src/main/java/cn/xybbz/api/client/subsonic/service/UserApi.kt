package cn.xybbz.api.client.subsonic.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.SystemInfoResponse
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 用户相关请求实体类
 */
interface UserApi : BaseApi {

    /**
     * POST PING系统
     * @return [String]
     */
    @POST("/rest/ping")
    suspend fun postPingSystem(): String

    /**
     * 获得服务端信息
     */
    @GET("/System/Info")
    suspend fun getSystemInfo(): SystemInfoResponse

}