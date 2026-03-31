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

package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.navidrome.data.PlaylistItemData
import cn.xybbz.api.client.navidrome.data.PlaylistRemoveMusicsUpdateResponse
import cn.xybbz.api.client.plex.data.PlexLibraryItemResponse
import cn.xybbz.api.client.plex.data.PlexPlaylistResponse
import cn.xybbz.api.client.plex.data.PlexResponse
import cn.xybbz.api.enums.plex.PlexPlaylistType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.parameters

class PlexPlaylistsApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun createPlaylist(
        title: String,
        type: String = PlexPlaylistType.AUDIO.toString(),
        smart: Int = 1,
    ): PlexResponse<PlexPlaylistResponse> {
        return httpClient.post("/playlists") {
            parametersXy {
                append("title", title)
                append("type", type)
                append("public", smart)
            }
        }.body()
    }

    suspend fun updatePlaylist(
        playlistId: String,
        title: String
    ): PlaylistItemData {
        return httpClient.put("/playlists/$playlistId") {
            parametersXy {
                append("title", title)
            }
        }.body()
    }

    suspend fun deletePlaylist(
        playlistId: String
    ) {
        httpClient.delete("/playlists/$playlistId")
    }

    suspend fun getPlaylists(
        order: PlexPlaylistType = PlexPlaylistType.AUDIO,
        smart: Int? = null,
        start: Int,
        pageSize: Int
    ): PlexResponse<PlexPlaylistResponse> {
        return httpClient.get("/playlists") {
            parametersXy {
                append("type", order.toString())
                append("smart", smart)
                append("X-Plex-Container-Start", start.toString())
                append("X-Plex-Container-Size", pageSize.toString())
            }
        }.body()
    }

    suspend fun getPlaylistById(
        id: String
    ): PlexResponse<PlexPlaylistResponse> {
        return httpClient.get("/playlists/$id").body()
    }

    suspend fun addPlaylistMusics(
        playlistId: String,
        uri: String
    ): PlexResponse<PlexPlaylistResponse> {
        return httpClient.put("/playlists/$playlistId/items") {
            parametersXy {
                append("uri", uri)
            }
        }.body()
    }

    suspend fun removePlaylistMusics(
        playlistId: String,
        itemId: String
    ): PlaylistRemoveMusicsUpdateResponse {
        return httpClient.delete("/playlists/$playlistId/items/$itemId").body()
    }

    suspend fun getPlaylistMusicList(
        playlistId: String,
        type: Int? = 10,
        start: Int,
        pageSize: Int,
        artistId: String? = null,
        albumId: String? = null,
        sort: String? = null,
        trackCollection: Int? = null,
        albumDecade: String? = null,
        albumStartYear: Int? = null,
        albumEndYear: Int? = null,
        params: Map<String, String>? = null
    ): PlexResponse<PlexLibraryItemResponse> {
        return httpClient.get("/playlists/$playlistId/items") {
            parametersXy {
                append("type", type)
                append("X-Plex-Container-Start", start.toString())
                append("X-Plex-Container-Size", pageSize.toString())
                append("artist.id", artistId)
                append("album.id", albumId)
                append("sort", sort)
                append("track.collection", trackCollection)
                append("album.decade", albumDecade)
                append("album.year>>=", albumStartYear)
                append("album.year<<=", albumEndYear)
                appendAll(params)
            }
        }.body()
    }
}