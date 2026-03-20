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
import cn.xybbz.api.client.navidrome.data.ArtistItem
import cn.xybbz.api.client.navidrome.data.FullResponse
import cn.xybbz.api.client.navidrome.data.toFullResponse
import cn.xybbz.api.client.subsonic.data.SubsonicArtistInfoResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters
import kotlinx.coroutines.NonCancellable.start

class NavidromeArtistsApi(private val httpClient: HttpClient) : BaseApi {
    suspend fun getArtists(
        start: Int,
        end: Int,
        order: OrderType = OrderType.ASC,
        sort: SortType = SortType.NAME,
        name: String? = null,
        missing: Boolean? = null,
        starred: Boolean? = null,
        libraryIds: List<String>? = null
    ): FullResponse<List<ArtistItem>?> {
        val httpResponse = httpClient.get("/api/artist") {
            parameters {
                append("_start", start.toString())
                append("_end", end.toString())
                append("_order", order.toString())
                append("_sort", sort.toString())
                append("name", name)
                append("missing", missing)
                append("starred", starred)
                appendAll("library_id", libraryIds)
            }
        }

        return httpResponse.toFullResponse()
    }

    suspend fun getArtist(artistId: String): ArtistItem? {
        return httpClient.get("/api/artist/${artistId}").body()
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