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
import cn.xybbz.api.client.subsonic.data.SubsonicArtistInfoResponse
import cn.xybbz.api.client.subsonic.data.SubsonicArtistResponse
import cn.xybbz.api.client.subsonic.data.SubsonicArtistsResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SubsonicArtistsApi : BaseApi{
    @GET("/rest/getArtists")
    suspend fun getArtists(
        @Query("musicFolderId") musicFolderId: String?,
    ): SubsonicResponse<SubsonicArtistsResponse>

    @GET("/rest/getArtist")
    suspend fun getArtist(@Query("id") id: String): SubsonicResponse<SubsonicArtistResponse>

    @GET("/rest/getArtistInfo2")
    suspend fun getArtistInfo(
        @Query("id") id: String,
        @Query("count") count: Int? = null
    ): SubsonicResponse<SubsonicArtistInfoResponse>
}