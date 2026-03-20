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

package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.CountsResponse
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import cn.xybbz.api.utils.toStringMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters
import io.ktor.util.appendAll

/**
 * 音乐,专辑,艺术家相关接口
 */
class EmbyItemApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 获得音乐列表
     * @param userId 用户id
     * @param itemRequest 请求参数
     * @return [Response<ItemResponse>]
     */
    suspend fun getUserItems(
        userId: String, itemRequest: ItemResponse
    ): Response<ItemResponse> {
        return httpClient.get("/emby/Users/${userId}/Items") {
            parameters{
                appendAll(itemRequest.toStringMap())
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
    ): Response<ItemResponse>{
        return httpClient.get("/emby/Items/${itemId}/Similar"){
            parameters{
                append("userId", userId)
                append("Limit", limit.toString())
                fields?.let { append("Fields", it) }
            }
        }.body()
    }

    /**
     * 获得数量统计
     */
    suspend fun getCounts(
        userId: String? = null,
        isFavorite: Boolean? = null
    ): CountsResponse{
        return httpClient.get("/emby/Items/Counts"){
            parameters{
                userId?.let { append("userId", it) }
                isFavorite?.let { append("isFavorite", it.toString()) }
            }
        }.body()
    }
}