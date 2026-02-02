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

package cn.xybbz.api.client.jellyfin.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CountsResponse(
    @param:Json(name = "MovieCount")
    val movieCount: Int,
    @param:Json(name = "SeriesCount")
    val seriesCount: Int,
    @param:Json(name = "EpisodeCount")
    val episodeCount: Int,
    @param:Json(name = "ArtistCount")
    val artistCount: Int,
    @param:Json(name = "ProgramCount")
    val programCount: Int,
    @param:Json(name = "TrailerCount")
    val trailerCount: Int,
    @param:Json(name = "SongCount")
    val songCount: Int,
    @param:Json(name = "AlbumCount")
    val albumCount: Int,
    @param:Json(name = "MusicVideoCount")
    val musicVideoCount: Int,
    @param:Json(name = "BoxSetCount")
    val boxSetCount: Int,
    @param:Json(name = "BookCount")
    val bookCount: Int,
    @param:Json(name = "ItemCount")
    val itemCount: Int
)
