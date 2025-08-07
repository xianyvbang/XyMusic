package cn.xybbz.api.client.navidrome.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.AuthenticateResponse
import cn.xybbz.api.client.navidrome.data.NavidromeLoginRequest
import cn.xybbz.api.client.navidrome.data.NavidromeLoginResponse
import cn.xybbz.api.client.navidrome.data.NavidromePingResponse
import cn.xybbz.api.client.subsonic.data.SubsonicParentResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

/**
 * 用户相关请求实体类
 */
interface NavidromeUserApi : BaseApi {


    /**
     * 按名称进行身份验证
     * @return [AuthenticateResponse]
     */
    @POST("/auth/login")
    suspend fun login(@Body loginRequest: NavidromeLoginRequest): NavidromeLoginResponse


    /**
     * POST PING系统
     * @return [String]
     */
    @POST("/rest/ping")
    suspend fun postPingSystem(): SubsonicResponse<NavidromePingResponse>

    /**
     * 上报播放记录
     */
    @GET("/rest/scrobble")
    suspend fun scrobble(@QueryMap scrobbleRequest: Map<String, String>): SubsonicResponse<SubsonicParentResponse>
}