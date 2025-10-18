package cn.xybbz.api.client.subsonic.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.subsonic.data.SubsonicParentResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

/**
 * 用户相关请求实体类
 */
interface SubsonicUserApi : BaseApi {

    /**
     * POST PING系统
     * @return [String]
     */
    @POST("/rest/ping")
    suspend fun postPingSystem(): SubsonicResponse<SubsonicParentResponse>

    /**
     * 上报播放记录
     */
    @GET("/rest/scrobble")
    suspend fun scrobble(@QueryMap scrobbleRequest: Map<String, String>):SubsonicResponse<SubsonicParentResponse>

}