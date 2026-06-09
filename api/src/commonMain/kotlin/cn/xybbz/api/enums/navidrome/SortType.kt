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

package cn.xybbz.api.enums.navidrome

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SortType(private val sortName: String) {
    @SerialName(value = "id")
    ID("id"),

    @SerialName(value = "updatedAt")
    UPDATED_AT("updatedAt"),

    @SerialName(value = "createdAt")
    CREATED_AT("createdAt"),

    @SerialName(value = "name")
    NAME("name"),

    @SerialName(value = "title")
    TITLE("title"),

    @SerialName(value = "sync")
    SYNC("sync"),

    @SerialName(value = "starred_at")
    STARRED_AT("starred_at"),

    /**
     * 最近添加
     */
    @SerialName(value = "recently_added")
    RECENTLY_ADDED("recently_added"),

    /**
     * 播放时间
     */
    @SerialName(value = "play_date")
    PLAY_DATE("play_date"),

    /**
     * 播放次数
     */
    @SerialName(value = "play_count")
    PLAY_COUNT("play_count"),

    @SerialName(value = "albumArtist")
    ALBUM_ARTIST("albumArtist"),

    @SerialName(value = "max_year")
    MAX_YEAR("max_year"),

    @SerialName(value = "album")
    ALBUM("album"),

    @SerialName(value = "artist")
    ARTIST("artist"),

    @SerialName(value = "year")
    YEAR("year"),

    @SerialName(value = "random")
    RANDOM("random")
    ;

    override fun toString(): String {
        return sortName
    }
}