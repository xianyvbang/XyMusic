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

import cn.xybbz.api.serializers.LocalDateTimeTimestampSerializer
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistID3(
    /**
     * 歌单编码
     */
    val id: String,
    /**
     * 歌单名称
     */
    val name: String,
    /**
     * 歌单评价
     */
    val comment: String? = null,
    /**
     * 所属用户
     */
    val owner: String,
    /**
     * 是否公开
     */
    val public: Boolean,
    /**
     * 歌曲数量
     */
    val songCount: Long,
    /**
     * 创建时间
     */
    @Serializable(LocalDateTimeTimestampSerializer::class)
    val created: Long,
    /**
     * 封面图片编码
     */
    val coverArt: String,
    /**
     * 专辑歌曲
     */
    val entry: List<SongID3>? = null
)
