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

package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemRequest
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import cn.xybbz.api.utils.toListMap
import cn.xybbz.api.utils.toStringMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters
import io.ktor.util.appendAll

class ArtistsApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 获得艺术家列表
     * @param [itemRequest] 请求信息
     * @return [Response<ItemResponse>]
     */
    suspend fun getArtists(
        itemRequest: ItemRequest
    ): Response<ItemResponse> {
        return httpClient.get("/Artists") {
            parametersXy {
                appendAll(*itemRequest.toListMap())
            }
        }.body()
    }

    /**
     * 相似歌手
     */
    suspend fun getSimilarArtists(
        artistId: String,
        itemRequest: ItemRequest
    ): Response<ItemResponse> {
        return httpClient.get("/Artists/$artistId/Similar") {
            parametersXy {
                appendAll(*itemRequest.toListMap())
            }
        }.body()
    }
}