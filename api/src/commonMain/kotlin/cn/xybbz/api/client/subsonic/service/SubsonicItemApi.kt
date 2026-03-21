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
import cn.xybbz.api.client.subsonic.data.SubsonicAlbumInfo2Response
import cn.xybbz.api.client.subsonic.data.SubsonicAlbumListResponse
import cn.xybbz.api.client.subsonic.data.SubsonicAlbumResponse
import cn.xybbz.api.client.subsonic.data.SubsonicRandomResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.client.subsonic.data.SubsonicScanStatusResponse
import cn.xybbz.api.client.subsonic.data.SubsonicSearchResponse
import cn.xybbz.api.client.subsonic.data.SubsonicSimilarSongsResponse
import cn.xybbz.api.client.subsonic.data.SubsonicSongResponse
import cn.xybbz.api.client.subsonic.data.SubsonicSongsByGenreResponse
import cn.xybbz.api.client.subsonic.data.SubsonicStarred2Response
import cn.xybbz.api.client.subsonic.data.SubsonicTopSongsResponse
import cn.xybbz.api.enums.subsonic.AlbumType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters

/**
 * 音乐,专辑,艺术家相关接口
 */
class SubsonicItemApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun getAlbumList2(
        type: AlbumType,
        size: Int,
        offset: Int = 0,
        fromYear: Int? = null,
        toYear: Int? = null,
        genre: String? = null,
        musicFolderId: String?,
    ): SubsonicResponse<SubsonicAlbumListResponse> {
        return httpClient.get("/rest/getAlbumList2") {
            parameters {
                append("type", type.toString())
                append("size", size)
                append("offset", offset)
                append("fromYear", fromYear)
                append("toYear", toYear)
                append("genre", genre)
                append("musicFolderId", musicFolderId)
            }
        }.body()
    }

    suspend fun getAlbum(id: String): SubsonicResponse<SubsonicAlbumResponse> {
        return httpClient.get("/rest/getAlbum") {
            parameters {
                append("id", id)
            }
        }.body()
    }

    suspend fun getAlbumInfo2(
        id: String
    ): SubsonicResponse<SubsonicAlbumInfo2Response> {
        return httpClient.get("/rest/getAlbumInfo2") {
            parameters {
                append("id", id)
            }
        }.body()
    }

    suspend fun getSong(id: String): SubsonicResponse<SubsonicSongResponse> {
        return httpClient.get("/rest/getSong") {
            parameters {
                append("id", id)
            }
        }.body()
    }

    suspend fun search3(
        query: String,
        artistCount: Int = 20,
        artistOffset: Int = 0,
        albumCount: Int = 20,
        albumOffset: Int = 0,
        songCount: Int = 20,
        songOffset: Int = 0,
        musicFolderId: String? = null
    ): SubsonicResponse<SubsonicSearchResponse> {
        return httpClient.get("/rest/search3") {
            parameters {
                append("query", query)
                append("artistCount", artistCount)
                append("artistOffset", artistOffset)
                append("albumCount", albumCount)
                append("albumOffset", albumOffset)
                append("songCount", songCount)
                append("songOffset", songOffset)
                append("musicFolderId", musicFolderId)
            }
        }.body()
    }

    suspend fun getRandomSongs(
        size: Int,
        genre: String? = null,
        fromYear: String? = null,
        toYear: String? = null,
        musicFolderId: String? = null
    ): SubsonicResponse<SubsonicRandomResponse> {
        return httpClient.get("/rest/getRandomSongs") {
            parameters {
                append("size", size)
                append("genre", genre)
                append("fromYear", fromYear)
                append("toYear", toYear)
                append("musicFolderId", musicFolderId)
            }
        }.body()
    }

    suspend fun getStarred2(
        musicFolderId: String?
    ): SubsonicResponse<SubsonicStarred2Response> {
        return httpClient.get("/rest/getStarred2") {
            parameters {
                append("musicFolderId", musicFolderId)
            }
        }.body()
    }

    suspend fun getSongsByGenre(
        genre: String,
        size: Int = 20,
        offset: Int = 0,
        musicFolderId: String? = null
    ): SubsonicResponse<SubsonicSongsByGenreResponse> {
        return httpClient.get("/rest/getSongsByGenre") {
            parameters {
                append("genre", genre)
                append("count", size)
                append("offset", offset)
                append("musicFolderId", musicFolderId)
            }
        }.body()
    }

    suspend fun getTopSongs(
        artistName: String,
        count: Int = 20
    ): SubsonicResponse<SubsonicTopSongsResponse> {
        return httpClient.get("/rest/getTopSongs") {
            parameters {
                append("artist", artistName)
                append("count", count)
            }
        }.body()
    }

    suspend fun getSimilarSongs(
        songId: String,
        count: Int = 20
    ): SubsonicResponse<SubsonicSimilarSongsResponse> {
        return httpClient.get("/rest/getSimilarSongs") {
            parameters {
                append("id", songId)
                append("count", count)
            }
        }.body()
    }

    suspend fun getScanStatus(): SubsonicResponse<SubsonicScanStatusResponse> {
        return httpClient.get("/rest/getScanStatus").body()
    }
}