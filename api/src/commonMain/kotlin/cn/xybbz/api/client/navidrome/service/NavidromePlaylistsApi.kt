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
import cn.xybbz.api.client.navidrome.data.FullResponse
import cn.xybbz.api.client.navidrome.data.NavidromeCreatePlaylistResponse
import cn.xybbz.api.client.navidrome.data.PlaylistAddMusicsUpdateRequest
import cn.xybbz.api.client.navidrome.data.PlaylistAddMusicsUpdateResponse
import cn.xybbz.api.client.navidrome.data.PlaylistCreateRequest
import cn.xybbz.api.client.navidrome.data.PlaylistItemData
import cn.xybbz.api.client.navidrome.data.PlaylistRemoveMusicsUpdateResponse
import cn.xybbz.api.client.navidrome.data.PlaylistUpdateRequest
import cn.xybbz.api.client.navidrome.data.SongItem
import cn.xybbz.api.client.navidrome.data.toFullResponse
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.parameters

class NavidromePlaylistsApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun createPlaylist(
        playlistCreateRequest: PlaylistCreateRequest
    ): NavidromeCreatePlaylistResponse? {
        return httpClient.post("/api/playlist") {
            postBlock { setBody(playlistCreateRequest) }
        }.body()
    }

    suspend fun updatePlaylist(
        playlistId: String,
        playlistUpdateRequest: PlaylistUpdateRequest
    ): PlaylistItemData {
        return httpClient.put("/api/playlist/$playlistId") {
            postBlock { setBody(playlistUpdateRequest) }
        }.body()
    }

    suspend fun deletePlaylist(
        playlistId: String
    ) {
        httpClient.delete("/api/playlist/$playlistId")
    }

    suspend fun getPlaylists(
        order: OrderType = OrderType.ASC,
        start: Int = 0,
        end: Int = 0,
        sort: SortType = SortType.ID,
        libraryIds: List<String>? = null
    ): FullResponse<List<PlaylistItemData>> {
        return httpClient.get("/api/playlist") {
            parameters {
                append("_order", order.toString())
                append("_start", start.toString())
                append("_end", end.toString())
                append("_sort", sort.toString())
                appendAll("library_id", libraryIds)

            }
        }.toFullResponse()
    }

    suspend fun addPlaylistMusics(
        playlistId: String,
        playlistAddMusicsRequest: PlaylistAddMusicsUpdateRequest
    ): PlaylistAddMusicsUpdateResponse {
        return httpClient.post("/api/playlist/$playlistId/tracks") {
            postBlock { setBody(playlistAddMusicsRequest) }
        }.body()
    }

    suspend fun removePlaylistMusics(
        playlistId: String,
        id: List<String>
    ): PlaylistRemoveMusicsUpdateResponse {
        return httpClient.delete("/api/playlist/$playlistId/tracks") {
            parameters {
                appendAll("id", id)
            }
        }.body()
    }

    suspend fun getPlaylistMusicList(
        playlistId: String,
        start: Int,
        end: Int,
        order: OrderType = OrderType.ASC,
        sort: SortType = SortType.TITLE,
        missing: Boolean? = null,
        starred: Boolean? = null
    ): FullResponse<List<SongItem>> {
        return httpClient.get("/api/playlist/$playlistId/tracks") {
            parameters {
                append("_start", start.toString())
                append("_end", end.toString())
                append("_order", order.toString())
                append("_sort", sort.toString())
                append("missing", missing)
                append("starred", starred)

            }
        }.toFullResponse()
    }
}