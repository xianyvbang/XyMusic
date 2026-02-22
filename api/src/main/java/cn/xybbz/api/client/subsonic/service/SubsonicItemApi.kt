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
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 音乐,专辑,艺术家相关接口
 */
interface SubsonicItemApi : BaseApi {

    @GET("/rest/getAlbumList2")
    suspend fun getAlbumList2(
        @Query("type") type: AlbumType,
        @Query("size") size: Int,
        @Query("offset") offset: Int = 0,
        @Query("fromYear") fromYear: Int? = null,
        @Query("toYear") toYear: Int? = null,
        @Query("genre") genre: String? = null,
        @Query("musicFolderId") musicFolderId: String?,
    ): SubsonicResponse<SubsonicAlbumListResponse>

    @GET("/rest/getAlbum")
    suspend fun getAlbum(@Query("id") id: String): SubsonicResponse<SubsonicAlbumResponse>

    @GET("/rest/getSong")
    suspend fun getSong(@Query("id") id: String): SubsonicResponse<SubsonicSongResponse>

    @GET("/rest/search3")
    suspend fun search3(
        @Query("query") query: String,
        @Query("artistCount") artistCount: Int = 20,
        @Query("artistOffset") artistOffset: Int = 0,
        @Query("albumCount") albumCount: Int = 20,
        @Query("albumOffset") albumOffset: Int = 0,
        @Query("songCount") songCount: Int = 20,
        @Query("songOffset") songOffset: Int = 0,
        @Query("musicFolderId") musicFolderId: String? = null
    ): SubsonicResponse<SubsonicSearchResponse>

    @GET("/rest/getRandomSongs")
    suspend fun getRandomSongs(
        @Query("size") size: Int,
        @Query("genre") genre: String? = null,
        @Query("fromYear") fromYear: String? = null,
        @Query("toYear") toYear: String? = null,
        @Query("musicFolderId") musicFolderId: String? = null
    ): SubsonicResponse<SubsonicRandomResponse>

    @GET("/rest/getStarred2")
    suspend fun getStarred2(
        @Query("musicFolderId") musicFolderId: String?
    ): SubsonicResponse<SubsonicStarred2Response>

    @GET("rest/getSongsByGenre")
    suspend fun getSongsByGenre(
        @Query("genre") genre: String,
        @Query("count") size: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("musicFolderId") musicFolderId: String? = null
    ):SubsonicResponse<SubsonicSongsByGenreResponse>

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

    @GET("/rest/getScanStatus")
    suspend fun getScanStatus(): SubsonicResponse<SubsonicScanStatusResponse>
}