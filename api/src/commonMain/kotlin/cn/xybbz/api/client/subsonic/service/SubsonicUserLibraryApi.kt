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
import cn.xybbz.api.client.jellyfin.data.FavoriteResponse
import cn.xybbz.api.client.subsonic.data.SubsonicDefaultResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters

class SubsonicUserLibraryApi(private val httpClient: HttpClient) : BaseApi {
    /**
     * 标记最喜欢项目
     * @return [FavoriteResponse]
     */
    suspend fun markFavoriteItem(
        id: List<String>? = null,
        albumId: List<String>? = null,
        artistId: List<String>? = null
    ): SubsonicResponse<SubsonicDefaultResponse> {
        return httpClient.get("/rest/star") {
            parameters {
                appendAll("id", id)
                appendAll("albumId", albumId)
                appendAll("artistId", artistId)
            }
        }.body()
    }

    /**
     * 取消标记喜欢物品
     * @return [FavoriteResponse]
     */
    suspend fun unmarkFavoriteItem(
        id: List<String>? = null,
        albumId: List<String>? = null,
        artistId: List<String>? = null
    ): SubsonicResponse<SubsonicDefaultResponse> {
        return httpClient.get("/rest/unstar") {
            parameters {
                appendAll("id", id)
                appendAll("albumId", albumId)
                appendAll("artistId", artistId)
            }
        }.body()
    }

}