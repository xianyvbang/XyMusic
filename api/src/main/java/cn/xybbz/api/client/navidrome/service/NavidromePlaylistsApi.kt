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
import cn.xybbz.api.client.navidrome.data.NavidromeCreatePlaylistResponse
import cn.xybbz.api.client.navidrome.data.PlaylistAddMusicsUpdateRequest
import cn.xybbz.api.client.navidrome.data.PlaylistAddMusicsUpdateResponse
import cn.xybbz.api.client.navidrome.data.PlaylistItemData
import cn.xybbz.api.client.navidrome.data.PlaylistRemoveMusicsUpdateResponse
import cn.xybbz.api.client.navidrome.data.PlaylistUpdateRequest
import cn.xybbz.api.client.navidrome.data.SongItem
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NavidromePlaylistsApi : BaseApi {

    @POST("/api/playlist")
    suspend fun createPlaylist(
        @Query("name") name: String? = null,
        @Query("public") public: Boolean = false
    ): NavidromeCreatePlaylistResponse?

    @PUT("/api/playlist/{playlistId}")
    suspend fun updatePlaylist(
        @Path("playlistId") playlistId: String,
        @Body playlistUpdateRequest: PlaylistUpdateRequest
    ): PlaylistItemData

    @DELETE("/api/playlist/{playlistId}")
    suspend fun deletePlaylist(
        @Query("playlistId") playlistId: String
    )

    @GET("/api/playlist")
    suspend fun getPlaylists(
        @Query("_order") order: OrderType = OrderType.ASC,
        @Query("_start") start: Int = 0,
        @Query("_end") end: Int = 0,
        @Query("_sort") sort: SortType = SortType.ID,
    ): Response<List<PlaylistItemData>>

    @POST("/api/playlist/{playlistId}/tracks")
    suspend fun addPlaylistMusics(
        @Path("playlistId") playlistId: String,
        @Body playlistAddMusicsRequest: PlaylistAddMusicsUpdateRequest
    ): PlaylistAddMusicsUpdateResponse

    @DELETE("/api/playlist/{playlistId}/tracks")
    suspend fun removePlaylistMusics(
        @Path("playlistId") playlistId: String,
        @Query("id") id: List<String>
    ): PlaylistRemoveMusicsUpdateResponse

    @GET("/api/playlist/{playlistId}/tracks")
    suspend fun getPlaylistMusicList(
        @Path("playlistId") playlistId: String,
        @Query("_start") start: Int,
        @Query("_end") end: Int,
        @Query("_order") order: OrderType = OrderType.ASC,
        @Query("_sort") sort: SortType = SortType.TITLE,
        @Query("missing") missing: Boolean? = null,
        @Query("starred") starred: Boolean? = null
    ): Response<List<SongItem>>
}