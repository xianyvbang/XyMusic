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

package cn.xybbz.api.client.subsonic.data

import kotlinx.serialization.Serializable


/**
 * ArtistID3，An artist from ID3 tags.
 */
@Serializable
data class ArtistID3 (
    /**
     * Artist album count.
     */
    val albumCount: Long? = null,

    /**
     * A covertArt id.
     */
    val coverArt: String? = null,

    /**
     * The id of the artist
     */
    val id: String,

    /**
     * The artist name.
     */
    val name: String,

    /**
     * 收藏时间 : 2026-01-28T02:00:15.76317727Z
     */
    val starred: String? = null,

    /**
     * 艺术家专辑
     */
    val album: List<AlbumID3>? = null
)
