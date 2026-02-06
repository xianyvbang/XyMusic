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

import cn.xybbz.api.enums.jellyfin.MediaType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Create new playlist dto.
 */
@Serializable
data class CreatePlaylistRequest(
    /**
     * The name of the new playlist.
     */
    @SerialName(value = "Name")
    val name: String? = null,
    /**
     * Item ids to add to the playlist.
     */
    @SerialName(value = "Ids")
    val ids: List<String>? = null,
    /**
     * The user id.
     */
    @SerialName(value = "UserId")
    val userId: String? = null,
    /**
     * The media type.
     */
    @SerialName(value = "MediaType")
    val mediaType: MediaType? = null,
    /**
     * The playlist users.
     */
    @SerialName(value = "Users")
    val users: List<PlaylistUserPermissions>? = null,
    /**
     * A value indicating whether the playlist is .
     */
    @SerialName(value = "IsPublic")
    val isPublic: Boolean? = false,
)

/**
 * Class to hold data on user permissions for playlists.
 */
@Serializable
data class PlaylistUserPermissions(
    /**
     * The user id.
     */
    @SerialName(value = "UserId")
    val userId: String,
    /**
     * A value indicating whether the user has edit permissions.
     */
    @SerialName(value = "CanEdit")
    val canEdit: Boolean,
)
