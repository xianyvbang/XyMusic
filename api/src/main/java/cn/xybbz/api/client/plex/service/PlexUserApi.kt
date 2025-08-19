package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.AuthenticateResponse
import cn.xybbz.api.client.jellyfin.data.LoginRequest
import cn.xybbz.api.client.plex.data.PlexLoginRequest
import cn.xybbz.api.client.plex.data.PlexLoginResponse
import cn.xybbz.api.client.plex.data.PlexPingSystemResponse
import cn.xybbz.api.client.plex.data.PlexSystemInfoResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface PlexUserApi : BaseApi {

    /**
     * 按名称进行身份验证
     * @param [LoginRequest]
     * @return [AuthenticateResponse]
     */
    @POST
    suspend fun authenticateByName(
        @Url fullUrl: String,
        @Body loginRequest: PlexLoginRequest
    ): PlexLoginResponse


    /**
     * POST PING系统
     * @return [String]
     */
    @POST("/")
    suspend fun postPingSystem(): PlexPingSystemResponse

    /**
     * 获得服务端信息
     */
    @GET
    suspend fun getSystemInfo(@Url fullUrl: String): List<PlexSystemInfoResponse>
}