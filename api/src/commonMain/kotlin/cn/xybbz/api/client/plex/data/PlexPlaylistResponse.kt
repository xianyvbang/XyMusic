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

package cn.xybbz.api.client.plex.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * plex歌单信息
 */
@Serializable
data class PlexPlaylistResponse(
    @SerialName(value = "Metadata")
    val metadata: List<PlaylistMetadatum>?,
    override val size: Int? = null,
    override val totalSize: Int? = null
) : PlexParentResponse()


@Serializable
data class PlaylistMetadatum(
    val addedAt: Long? = null,
    val composite: String? = null,
    val duration: Long? = null,
    val guid: String? = null,
    val icon: String? = null,
    val key: String,
    val lastViewedAt: Long? = null,
    val leafCount: Long? = null,
    val playlistType: String? = null,
    val ratingKey: String,
    val smart: Boolean? = null,
    val summary: String? = null,
    val title: String? = null,
    val type: String? = null,
    val updatedAt: Long? = null,
    val viewCount: Long? = null
)