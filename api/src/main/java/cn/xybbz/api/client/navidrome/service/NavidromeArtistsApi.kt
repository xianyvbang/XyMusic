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
import cn.xybbz.api.client.navidrome.data.ArtistItem
import cn.xybbz.api.client.subsonic.data.SubsonicArtistInfoResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NavidromeArtistsApi : BaseApi{
    @GET("/api/artist")
    suspend fun getArtists(
        @Query("_start") start: Int,
        @Query("_end") end: Int,
        @Query("_order") order: OrderType = OrderType.ASC,
        @Query("_sort") sort: SortType = SortType.NAME,
        @Query("name") name: String? = null,
        @Query("missing") missing: Boolean? = null,
        @Query("starred") starred: Boolean? = null
    ): Response<List<ArtistItem>?>

    @GET("/api/artist/{artistId}")
    suspend fun getArtist(@Path("artistId") artistId: String): ArtistItem?

    @GET("/rest/getArtistInfo2")
    suspend fun getArtistInfo(
        @Query("id") id: String,
        @Query("count") count: Int? = null
    ): SubsonicResponse<SubsonicArtistInfoResponse>

}