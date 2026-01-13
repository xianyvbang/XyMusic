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
import cn.xybbz.api.client.navidrome.data.AlbumItem
import cn.xybbz.api.client.navidrome.data.SongItem
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.client.subsonic.data.SubsonicSearchResponse
import cn.xybbz.api.client.subsonic.data.SubsonicSimilarSongsResponse
import cn.xybbz.api.client.subsonic.data.SubsonicStarred2Response
import cn.xybbz.api.client.subsonic.data.SubsonicTopSongsResponse
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 音乐,专辑,艺术家相关接口
 */
interface NavidromeItemApi : BaseApi {

    @GET("/api/album")
    suspend fun getAlbumList(
        @Query("_start") start: Int,
        @Query("_end") end: Int,
        @Query("_order") order: OrderType = OrderType.ASC,
        @Query("_sort") sort: SortType = SortType.NAME,
        @Query("name") name: String? = null,
        @Query("missing") missing: Boolean? = null,
        @Query("starred") starred: Boolean? = null,
        @Query("year") year: Int? = null,
        @Query("genre_id") genreId: String? = null,
        @Query("artist_id") artistId: String? = null,
        //是否最近播放
        @Query("recently_played") recentlyPlayed: Boolean? = null,
    ): Response<List<AlbumItem>>

    @GET("/api/album")
    suspend fun getAlbum(@Query("id") id: String): AlbumItem

    @GET("/api/song")
    suspend fun getSong(
        @Query("_start") start: Int,
        @Query("_end") end: Int,
        @Query("_order") order: OrderType = OrderType.ASC,
        @Query("_sort") sort: SortType = SortType.TITLE,
        @Query("title") title: String? = null,
        @Query("missing") missing: Boolean? = null,
        @Query("starred") starred: Boolean? = null,
        @Query("genre_id") genreIds: List<String>? = null,
        @Query("album_id") albumId: String? = null,
        @Query("artist_id") artistIds: List<String>? = null,
        @Query("year") year: Int? = null
    ): Response<List<SongItem>>

    @GET("/rest/getTopSongs")
    suspend fun getTopSongs(
        @Query("artist") artistName: String,
        @Query("count") count: Int = 20
    ): SubsonicResponse<SubsonicTopSongsResponse>

    @GET("/rest/getSimilarSongs")
    suspend fun getSimilarSongs(
        @Query("id") songId: String,
        @Query("count") count: Int = 20
    ): SubsonicResponse<SubsonicSimilarSongsResponse>
}