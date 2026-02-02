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

import com.squareup.moshi.JsonClass

/**
 * AlbumID3，Album with songs.
 */
@JsonClass(generateAdapter = true)
data class AlbumID3  (
    /**
     * 专辑编码
     */
    val id: String,
    /**
     * 专辑艺术家编码
     */
    val artistId: String,
    /**
     * 专辑名称
     */
    val name: String,
    /**
     * 艺术家名称
     */
    val artist: String,
    /**
     * 创建时间
     */
    val created: String,
    /**
     * 持续时间
     */
    val duration: Float,
    /**
     * 歌曲数量
     */
    val songCount: Long,
    /**
     * 封面图片编码
     */
    val coverArt: String? = null,
    /**
     * 专辑歌曲
     */
    val song: List<SongID3>? = null,
    /**
     * 收藏时间 : 2026-01-28T02:00:15.76317727Z
     */
    val starred: String? = null,
)