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
import cn.xybbz.api.client.jellyfin.data.CountsResponse
import cn.xybbz.api.client.jellyfin.data.ItemRequest
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import cn.xybbz.api.utils.toListMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters
import io.ktor.http.parseQueryString
import io.ktor.util.appendAll

/**
 * 音乐,专辑,艺术家相关接口
 */
class ItemApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 获取音频列表
     * @param [itemRequest] 物品请求
     * @return [Response<ItemResponse>]
     */
    suspend fun getItems(itemRequest: ItemRequest): Response<ItemResponse> {
        return httpClient.get("/Items") {
            url {
                parameters.appendAll(*itemRequest.toListMap())
            }

        }.body()
    }


    /**
     * 获得相似歌曲
     */
    suspend fun getSimilarItems(
        itemId: String,
        userId: String,
        limit: Int,
        fields: String? = null
    ): Response<ItemResponse> {
        return httpClient.get("/Items/$itemId/Similar") {
            parameters {
                append("userId", userId)
                append("Limit", limit.toString())
                append("Fields", fields)
            }
        }.body()
    }

    /**
     * 获得数量统计
     */
    suspend fun getCounts(
        userId: String? = null,
        isFavorite: Boolean? = null
    ): CountsResponse {
        return httpClient.get("/Items/Counts") {
            parameters {
                append("userId", userId)
                append("isFavorite",isFavorite)
            }
        }.body()
    }
}