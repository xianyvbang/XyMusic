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
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ArtistsApi : BaseApi {

    /**
     * 获得艺术家列表
     * @param [itemRequest] 请求信息
     * @return [Response<ItemResponse>]
     */
    @GET("/Artists")
    suspend fun getArtists(
        @QueryMap itemRequest: Map<String, String>?,
    ): Response<ItemResponse>

    /**
     * 相似歌手
     */
    @GET("/Artists/{artistId}/Similar")
    suspend fun getSimilarArtists(
        @Path("artistId") artistId: String,
        @QueryMap itemRequest: Map<String, String>?
    ): Response<ItemResponse>
}