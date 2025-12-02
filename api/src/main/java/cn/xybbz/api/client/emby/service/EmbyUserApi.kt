package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.AuthenticateResponse
import cn.xybbz.api.client.jellyfin.data.LoginRequest
import cn.xybbz.api.client.jellyfin.data.PlaybackStartInfo
import cn.xybbz.api.client.jellyfin.data.SystemInfoResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EmbyUserApi : BaseApi {

    /**
     * 按用户名和米面进行身份验证
     * @param [LoginRequest]
     * @return [AuthenticateResponse]
     */
    @POST("/emby/Users/AuthenticateByName")
    suspend fun authenticateByName(@Body loginRequest: LoginRequest): AuthenticateResponse


    /**
     * POST PING系统
     * @return [String]
     */
    @POST("/emby/System/Ping")
    suspend fun postPingSystem(): Response<ResponseBody>

    /**
     * 获得服务端信息
     */
    @GET("/emby/System/Info")
    suspend fun getSystemInfo(): SystemInfoResponse

    /**
     * 上报正在播放音乐
     * @param [data] 数据
     */
    @POST("/emby/Sessions/Playing")
    suspend fun playing(@Body data: PlaybackStartInfo)

    /**
     * 上报播放进度
     * @param [data] 数据
     */
    @POST("/emby/Sessions/Playing/Progress")
    suspend fun progress(@Body data: PlaybackStartInfo)
}