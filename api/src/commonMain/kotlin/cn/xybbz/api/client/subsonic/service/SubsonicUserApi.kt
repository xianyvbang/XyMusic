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

package cn.xybbz.api.client.subsonic.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.subsonic.data.SubsonicDefaultResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.client.subsonic.data.SubsonicUserResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
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
    suspend fun postPingSystem(): SubsonicResponse<SubsonicDefaultResponse>

    /**
     * 获取用户信息
     */
    @GET("/rest/getUser")
    suspend fun getUser(@Query("username") username: String): SubsonicResponse<SubsonicUserResponse>

    /**
     * 上报播放记录
     */
    @GET("/rest/scrobble")
    suspend fun scrobble(@QueryMap scrobbleRequest: Map<String, String>):SubsonicResponse<SubsonicDefaultResponse>

}