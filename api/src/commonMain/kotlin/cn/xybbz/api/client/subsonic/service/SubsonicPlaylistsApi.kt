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
import cn.xybbz.api.client.subsonic.data.SubsonicPlaylistResponse
import cn.xybbz.api.client.subsonic.data.SubsonicPlaylistsResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters

class SubsonicPlaylistsApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun createPlaylist(
        playlistId: String? = null,
        name: String? = null,
        songId: List<String>? = null
    ): SubsonicResponse<SubsonicPlaylistResponse> {
        return httpClient.get("/rest/createPlaylist") {
            parameters {
                append("playlistId", playlistId)
                append("name", name)
                appendAll("songId", songId)
            }
        }.body()
    }

    suspend fun updatePlaylist(
        playlistId: String,
        name: String? = null,
        comment: String? = null,
        public: Boolean? = false,
        songIdToAdd: List<String>? = null,
        songIndexToRemove: List<String>? = null
    ): SubsonicResponse<SubsonicDefaultResponse> {
        return httpClient.get("/rest/updatePlaylist") {
            parameters {
                append("playlistId", playlistId)
                append("name", name)
                append("comment", comment)
                append("public", public)
                appendAll("songIdToAdd", songIdToAdd)
                appendAll("songIndexToRemove", songIndexToRemove)
            }
        }.body()
    }

    suspend fun deletePlaylist(
        id: String
    ): SubsonicResponse<SubsonicDefaultResponse> {
        return httpClient.get("/rest/deletePlaylist") {
            parameters {
                append("id", id)
            }
        }.body()
    }


    suspend fun getPlaylists(username: String): SubsonicResponse<SubsonicPlaylistsResponse> {
        return httpClient.get("/rest/getPlaylists") {
            parameters {
                append("username", username)
            }
        }.body()
    }

    suspend fun getPlaylistById(
        id: String
    ): SubsonicResponse<SubsonicPlaylistResponse> {
        return httpClient.get("/rest/getPlaylist") {
            parameters {
                append("id", id)
            }
        }.body()
    }

}