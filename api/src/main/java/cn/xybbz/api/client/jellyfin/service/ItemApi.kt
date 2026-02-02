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
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * 音乐,专辑,艺术家相关接口
 */
interface ItemApi : BaseApi {

    /**
     * 获取音频列表
     * @param [itemRequest] 物品请求
     * @return [Response<ItemResponse>]
     */
    @GET("/Items")
    suspend fun getItems(@QueryMap itemRequest: Map<String, String>): Response<ItemResponse>


    /**
     * 获得相似歌曲
     */
    @GET("/Items/{itemId}/Similar")
    suspend fun getSimilarItems(
        @Path("itemId") itemId: String,
        @Query("userId") userId: String,
        @Query("Limit") limit: Int,
        @Query("Fields") fields: String? = null
    ): Response<ItemResponse>

    /**
     * 获得数量统计
     */
    @GET("/Items/Counts")
    suspend fun getCounts(
        @Query("userId") userId: String? = null,
        @Query("isFavorite") isFavorite: Boolean? = null
    ): CountsResponse
}