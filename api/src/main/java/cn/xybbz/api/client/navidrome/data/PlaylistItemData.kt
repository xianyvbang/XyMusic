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

package cn.xybbz.api.client.navidrome.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlaylistItemData(
    val id: String,
    /**
     * 名称
     */
    val name: String,
    /**
     * 描述
     */
    val comment: String,
    /**
     * 时长,单位秒
     */
    val duration: Double,
    /**
     * 大小 字节
     */
    val size: Long,
    /**
     * 歌曲数量
     */
    val songCount: Long,
    /**
     * 所属人员
     */
    val ownerName: String,
    /**
     * 所属人员id
     */
    val ownerId: String,
    /**
     * 是否公开
     */
    val public: Boolean,
    /**
     * 所属路径
     */
    val path: String,
    /**
     * 是否为自动导入
     */
    val sync: Boolean,
    /**
     * 创建时间
     */
    val createdAt: String,
    /**
     * 更新时间
     */
    val updatedAt: String
)
