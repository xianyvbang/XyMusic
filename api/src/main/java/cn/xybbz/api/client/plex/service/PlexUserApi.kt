package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.AuthenticateResponse
import cn.xybbz.api.client.plex.data.PlexLoginRequest
import cn.xybbz.api.client.plex.data.PlexLoginResponse
import cn.xybbz.api.client.plex.data.PlexPingSystemResponse
import cn.xybbz.api.client.plex.data.PlexSystemInfoResponse
import cn.xybbz.api.enums.plex.PlayState
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface PlexUserApi : BaseApi {

    /**
     * 按名称进行身份验证
     * @param [PlexLoginRequest]
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
    @GET("/")
    suspend fun postPingSystem(): PlexPingSystemResponse

    /**
     * 获得服务端信息
     */
    @GET
    suspend fun getSystemInfo(@Url fullUrl: String): List<PlexSystemInfoResponse>


    /**
     * 上报正在播放音乐
     */
    @GET("/:/timeline")
    suspend fun playing(
        @Query("ratingKey") ratingKey: String,
        @Query("time") time: Long,
        @Query("state") state: PlayState
    )
}