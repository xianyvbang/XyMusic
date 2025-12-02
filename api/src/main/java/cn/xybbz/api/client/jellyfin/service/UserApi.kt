package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.AuthenticateResponse
import cn.xybbz.api.client.jellyfin.data.LoginRequest
import cn.xybbz.api.client.jellyfin.data.PlaybackStartInfo
import cn.xybbz.api.client.jellyfin.data.SystemInfoResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

/**
 * 用户相关请求实体类
 */
interface UserApi : BaseApi {

    /**
     * 按用户名和密码进行身份验证
     * @param [loginRequest] 登录请求
     * @return [AuthenticateResponse]
     */
    @POST("/Users/AuthenticateByName")
    suspend fun authenticateByName(@Body loginRequest: LoginRequest): AuthenticateResponse

    /**
     * POST PING系统
     * @return [String]
     */
    @POST("/System/Ping")
    suspend fun postPingSystem(): String

    /**
     * 获得服务端信息
     */
    @GET("/System/Info")
    suspend fun getSystemInfo(): SystemInfoResponse

    /**
     * 上报正在播放音乐
     */
    @POST("/Sessions/Playing")
    suspend fun playing(@Body data: PlaybackStartInfo)

    /**
     * 上报播放进度
     */
    @POST("/emby/Sessions/Playing/Progress")
    suspend fun progress(@Body data: PlaybackStartInfo)

}