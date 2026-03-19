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

package cn.xybbz.api.enums.plex

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * The type of media content in the Plex library. This can represent videos, music, or
 * photos.
 */
@Serializable
enum class MetadatumType(val value: String) {
    @SerialName(value= "album")
    Album("album"),
    @SerialName(value= "artist")
    Artist("artist"),
    @SerialName(value= "collection")
    Collection("collection"),
    @SerialName(value= "episode")
    Episode("episode"),
    @SerialName(value= "movie")
    Movie("movie"),
    @SerialName(value= "photo")
    Photo("photo"),
    @SerialName(value= "photoalbum")
    Photoalbum("photoalbum"),
    @SerialName(value= "season")
    Season("season"),
    @SerialName(value= "show")
    Show("show"),
    @SerialName(value= "track")
    Track("track"),
    @SerialName(value= "genre")
    Genre("genre")
    ;

    override fun toString(): String {
        return value
    }
}