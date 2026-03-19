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

package cn.xybbz.api.enums.jellyfin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum ItemFilter.
 */
@Serializable
enum class ItemFilter(
    private val serialName: String,
) {
    @SerialName(value = "IsFolder")
    IS_FOLDER("IsFolder"),

    @SerialName(value = "IsNotFolder")
    IS_NOT_FOLDER("IsNotFolder"),

    @SerialName(value = "IsUnplayed")
    IS_UNPLAYED("IsUnplayed"),

    @SerialName(value = "IsPlayed")
    IS_PLAYED("IsPlayed"),

    @SerialName(value = "IsFavorite")
    IS_FAVORITE("IsFavorite"),

    @SerialName(value = "IsResumable")
    IS_RESUMABLE("IsResumable"),

    @SerialName(value = "Likes")
    LIKES("Likes"),

    @SerialName(value = "Dislikes")
    DISLIKES("Dislikes"),

    @SerialName(value = "IsFavoriteOrLikes")
    IS_FAVORITE_OR_LIKES("IsFavoriteOrLikes"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}