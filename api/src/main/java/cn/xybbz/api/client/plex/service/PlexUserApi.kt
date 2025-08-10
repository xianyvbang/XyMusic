package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.AuthenticateResponse
import cn.xybbz.api.client.jellyfin.data.LoginRequest
import cn.xybbz.api.client.jellyfin.data.PlaybackStartInfo
import cn.xybbz.api.client.plex.data.PlexLoginRequest
import cn.xybbz.api.client.plex.data.PlexLoginResponse
import cn.xybbz.api.client.plex.data.PlexSystemInfoResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap
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
    @POST("/emby/System/Ping")
    suspend fun postPingSystem(): Response<ResponseBody>

    /**
     * 获得服务端信息
     */
    @GET
    suspend fun getSystemInfo(@Url fullUrl: String): PlexSystemInfoResponse

    /**
     * 上报正在播放音乐
     */
    @POST("/emby/Sessions/Playing")
    suspend fun playing(@Body data: PlaybackStartInfo)

    /**
     * 上报播放进度
     */
    @POST("/emby/Sessions/Playing/Progress")
    suspend fun progress(@Body data: PlaybackStartInfo)

    /**
     * 获得播放地址
     */
    @GET("/emby/Audio/{itemId}/stream")
    suspend fun getAudioStream(
        @Path("itemId") itemId: String,
        @QueryMap getAudioStreamRequest: Map<String, String>
    ): String
}