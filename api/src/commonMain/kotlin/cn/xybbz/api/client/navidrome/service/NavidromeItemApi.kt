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
import cn.xybbz.api.client.navidrome.data.FullResponse
import cn.xybbz.api.client.navidrome.data.SongItem
import cn.xybbz.api.client.navidrome.data.toFullResponse
import cn.xybbz.api.client.subsonic.data.SubsonicAlbumInfo2Response
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.client.subsonic.data.SubsonicSimilarSongsResponse
import cn.xybbz.api.client.subsonic.data.SubsonicTopSongsResponse
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters

/**
 * 音乐,专辑,艺术家相关接口
 */
class NavidromeItemApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun getAlbumList(
        start: Int,
        end: Int,
        order: OrderType = OrderType.ASC,
        sort: SortType = SortType.NAME,
        name: String? = null,
        missing: Boolean? = null,
        starred: Boolean? = null,
        year: Int? = null,
        genreId: String? = null,
        artistId: String? = null,
        //是否最近播放
        recentlyPlayed: Boolean? = null,
        libraryIds: List<String>? = null
    ): FullResponse<List<AlbumItem>> {
        return httpClient.get("/api/album") {
            parametersXy {
                append("_start", start.toString())
                append("_end", end.toString())
                append("_order", order.toString())
                append("_sort", sort.toString())
                append("name", name)
                append("missing", missing)
                append("starred", starred)
                append("year", year)
                append("genre_id", genreId)
                append("artist_id", artistId)
                append("recently_played", recentlyPlayed)
                appendAll("library_id", libraryIds)
            }

        }.toFullResponse()
    }

    suspend fun getAlbum(id: String): AlbumItem {
        return httpClient.get("/api/album/$id").body()
    }

    suspend fun getAlbumInfo2(
        id: String
    ): SubsonicResponse<SubsonicAlbumInfo2Response> {
        return httpClient.get("/rest/getAlbumInfo2") {
            parametersXy {
                append("id", id)
            }
        }.body()
    }

    suspend fun getSong(
        start: Int,
        end: Int,
        order: OrderType = OrderType.ASC,
        sort: SortType = SortType.TITLE,
        title: String? = null,
        missing: Boolean? = null,
        starred: Boolean? = null,
        genreIds: List<String>? = null,
        albumId: String? = null,
        artistIds: List<String>? = null,
        year: Int? = null,
        libraryIds: List<String>? = null
    ): FullResponse<List<SongItem>> {
        return httpClient.get("/api/song") {
            parametersXy {
                append("_start", start.toString())
                append("_end", end.toString())
                append("_order", order.toString())
                append("_sort", sort.toString())
                append("title", title)
                append("missing", missing)
                append("starred", starred)
                appendAll("genre_id", genreIds)
                append("album_id", albumId)
                appendAll("artist_id", artistIds)
                append("year", year)
                appendAll("library_id", libraryIds)
            }
        }.toFullResponse()
    }

    suspend fun getTopSongs(
        artistName: String,
        count: Int = 20,
        libraryIds: List<String>? = null
    ): SubsonicResponse<SubsonicTopSongsResponse> {
        return httpClient.get("/rest/getTopSongs") {
            parametersXy {
                append("artist", artistName)
                append("count", count.toString())
                appendAll("library_id", libraryIds)
            }
        }.body()
    }

    suspend fun getSimilarSongs(
        songId: String,
        count: Int = 20,
        libraryIds: List<String>? = null
    ): SubsonicResponse<SubsonicSimilarSongsResponse> {
        return httpClient.get("/rest/getSimilarSongs") {
            parametersXy {
                append("id", songId)
                append("count", count.toString())
                appendAll("library_id", libraryIds)
            }
        }.body()
    }
}