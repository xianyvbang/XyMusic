/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.api.client.navidrome.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.AuthenticateResponse
import cn.xybbz.api.client.navidrome.data.NavidromeLoginRequest
import cn.xybbz.api.client.navidrome.data.NavidromeLoginResponse
import cn.xybbz.api.client.navidrome.data.NavidromePingResponse
import cn.xybbz.api.client.navidrome.data.TranscodingInfo
import cn.xybbz.api.client.subsonic.data.SubsonicParentResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
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

    /**
     * 获得转码信息
     */
    @GET("/api/transcoding")
    suspend fun getTranscodingInfo(
        @Query("_start")start:Int=0,
        @Query("_end")end:Int=1000,
        @Query("_order") order: OrderType = OrderType.ASC,
        @Query("_sort") sort: SortType = SortType.NAME,
    ): Response<List<TranscodingInfo>>
}