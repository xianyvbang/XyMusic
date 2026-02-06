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
import cn.xybbz.api.client.subsonic.data.SubsonicDefaultResponse
import cn.xybbz.api.client.subsonic.data.SubsonicParentResponse
import cn.xybbz.api.client.subsonic.data.SubsonicPlaylistResponse
import cn.xybbz.api.client.subsonic.data.SubsonicPlaylistsResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SubsonicPlaylistsApi : BaseApi {

    @GET("/rest/createPlaylist")
    suspend fun createPlaylist(
        @Query("playlistId") playlistId: String? = null,
        @Query("name") name: String? = null,
        @Query("songId") songId: List<String>? = null
    ): SubsonicResponse<SubsonicPlaylistResponse>

    @GET("/rest/updatePlaylist")
    suspend fun updatePlaylist(
        @Query("playlistId") playlistId: String,
        @Query("name") name: String? = null,
        @Query("comment") comment: String? = null,
        @Query("public") public: Boolean? = false,
        @Query("songIdToAdd") songIdToAdd: List<String>? = null,
        @Query("songIndexToRemove") songIndexToRemove: List<String>? = null
    ): SubsonicResponse<SubsonicDefaultResponse>

    @GET("/rest/deletePlaylist")
    suspend fun deletePlaylist(
        @Query("id") id: String
    ): SubsonicResponse<SubsonicDefaultResponse>

    @GET("/rest/getPlaylists")
    suspend fun getPlaylists(@Query("username") username: String): SubsonicResponse<SubsonicPlaylistsResponse>

    @GET("/rest/getPlaylist")
    suspend fun getPlaylistById(
        @Query("id") id: String
    ): SubsonicResponse<SubsonicPlaylistResponse>

}