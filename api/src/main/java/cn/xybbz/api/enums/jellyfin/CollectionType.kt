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
 * Collection type.
 */
@Serializable
enum class CollectionType(
    val serialName: String,
) {
    @SerialName(value = "unknown")
    UNKNOWN("unknown"),

    @SerialName(value = "movies")
    MOVIES("movies"),

    @SerialName(value = "tvshows")
    TVSHOWS("tvshows"),

    @SerialName(value = "music")
    MUSIC("music"),

    @SerialName(value = "musicvideos")
    MUSICVIDEOS("musicvideos"),

    @SerialName(value = "trailers")
    TRAILERS("trailers"),

    @SerialName(value = "homevideos")
    HOMEVIDEOS("homevideos"),

    @SerialName(value = "boxsets")
    BOXSETS("boxsets"),

    @SerialName(value = "books")
    BOOKS("books"),

    @SerialName(value = "photos")
    PHOTOS("photos"),

    @SerialName(value = "livetv")
    LIVETV("livetv"),

    @SerialName(value = "playlists")
    PLAYLISTS("playlists"),

    @SerialName(value = "folders")
    FOLDERS("folders"),

    @SerialName(value = "otherVideos")
    OTHER_VIDEOS("otherVideos"),

    @SerialName(value = "people")
    PEOPLE("people"),

    @SerialName(value = "tv")
    TV("tv"),

    @SerialName(value = "artist")
    ARTIST("artist")
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}