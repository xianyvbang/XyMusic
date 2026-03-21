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
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters

class SubsonicArtistsApi(private val httpClient: HttpClient) : BaseApi {
    suspend fun getArtists(
        musicFolderId: String?,
    ): SubsonicResponse<SubsonicArtistsResponse> {
        return httpClient.get("/rest/getArtists") {
            parameters {
                append("musicFolderId", musicFolderId)
            }
        }.body()
    }

    suspend fun getArtist(id: String): SubsonicResponse<SubsonicArtistResponse> {
        return httpClient.get("/rest/getArtist") {
            parameters {
                append("id", id)
            }
        }.body()
    }

    suspend fun getArtistInfo(
        id: String,
        count: Int? = null
    ): SubsonicResponse<SubsonicArtistInfoResponse> {
        return httpClient.get("/rest/getArtistInfo2") {
            parameters {
                append("id", id)
                append("count", count)
            }
        }.body()
    }
}