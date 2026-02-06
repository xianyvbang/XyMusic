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

@Serializable
enum class PlexSortType(private val sortName: String) {
    /**
     * 播放次数
     */
    @SerialName(value = "viewCount")
    VIEWCOUNT("viewCount"),

    @SerialName(value = "titleSort")
    TITLE_SORT("titleSort"),


    @SerialName(value = "lastViewedAt")
    LAST_VIEWED_AT("lastViewedAt"),

    @SerialName(value = "addedAt")
    ADDED_AT("addedAt"),

    @SerialName(value = "updatedAt")
    UPDATED_AT("updatedAt"),

    @SerialName(value = "year")
    YEAR("year"),

    @SerialName(value = "random")
    RANDOM("random"),

    @SerialName(value = "artist.titleSort")
    ARTIST_TITLE_SORT("artist.titleSort"),
    @SerialName(value = "ratingCount")
    RATING_COUNT("ratingCount"),
    @SerialName(value = "album.titleSort")
    ALBUM_TITLE_SORT("album.titleSort")
    ;

    override fun toString(): String {
        return sortName
    }
}