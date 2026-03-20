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
import cn.xybbz.api.client.navidrome.data.FullResponse
import cn.xybbz.api.client.navidrome.data.NavidromeLoginRequest
import cn.xybbz.api.client.navidrome.data.NavidromeLoginResponse
import cn.xybbz.api.client.navidrome.data.TranscodingInfo
import cn.xybbz.api.client.navidrome.data.toFullResponse
import cn.xybbz.api.client.subsonic.data.ScrobbleRequest
import cn.xybbz.api.client.subsonic.data.SubsonicDefaultResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.client.subsonic.data.SubsonicUserResponse
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import cn.xybbz.api.utils.toListMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.parameters
import io.ktor.util.appendAll

/**
 * 用户相关请求实体类
 */
class NavidromeUserApi(private val httpClient: HttpClient) : BaseApi {


    /**
     * 按名称进行身份验证
     * @return [AuthenticateResponse]
     */
    suspend fun login(loginRequest: NavidromeLoginRequest): NavidromeLoginResponse {
        return httpClient.post("/auth/login") {
            postBlock { setBody(loginRequest) }
        }.body()
    }

    /**
     * 获取用户信息
     */
    suspend fun getUser(username: String): SubsonicResponse<SubsonicUserResponse> {
        return httpClient.get("/rest/getUser") {
            parameters {
                append("username", username)
            }
        }.body()
    }

    /**
     * POST PING系统
     * @return [String]
     */
    suspend fun postPingSystem(): SubsonicResponse<SubsonicDefaultResponse> {
        return httpClient.post("/rest/ping") {}.body()
    }

    /**
     * 上报播放记录
     */
    suspend fun scrobble(scrobbleRequest: ScrobbleRequest): SubsonicResponse<SubsonicDefaultResponse> {
        return httpClient.get("/rest/scrobble") {
            parameters {
                appendAll(*scrobbleRequest.toListMap())
            }
        }.body()
    }

    /**
     * 获得转码信息
     */
    suspend fun getTranscodingInfo(
        start: Int = 0,
        end: Int = 1000,
        order: OrderType = OrderType.ASC,
        sort: SortType = SortType.NAME,
    ): FullResponse<List<TranscodingInfo>> {
        return httpClient.get("/api/transcoding") {
            parameters {
                append("_start", start.toString())
                append("_end", end.toString())
                append("_order", order.toString())
                append("_sort", sort.toString())
            }
        }.toFullResponse()
    }
}