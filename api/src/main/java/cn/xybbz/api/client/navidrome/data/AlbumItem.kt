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

import kotlinx.serialization.Serializable

@Serializable
data class AlbumItem(


    val id: String,
    /**
     * 所属文档id
     */
    val libraryId: Long,
    /**
     * 名称
     */
    val name: String,
    /**
     * 播放次数
     */
    val playCount: Long? = null,
    /**
     * 播放时间
     */
    val playDate: String? = null,
    /**
     * 评价星级
     */
    val rating: Int? = null,
    /**
     * 是否收藏
     */
    val starred: Boolean? = null,
    /**
     * 收藏时间
     */
    val starredAt: String? = null,
    /**
     * 专辑艺术家id
     */
    val albumArtistId: String,
    /**
     * 专辑艺术家名称
     */
    val albumArtist: String,
    /**
     * 最大年份
     */
    val maxYear: Int,
    /**
     * 最小年份
     */
    val minYear: Int,
    /**
     * 年份
     */
    val date: String? = null,
    /**
     * 最大原发行年份
     */
    val maxOriginalYear: Int,
    /**
     * 最小原发行年份
     */
    val minOriginalYear: Int,
    /**
     * 是否能编辑
     */
    val compilation: Boolean,
    /**
     * 描述
     */
    val comment: String? = "",
    /**
     * 音乐数量
     */
    val songCount: Long,
    /**
     * 时长/秒
     */
    val duration: Double,
    /**
     * 大小/字节
     */
    val size: Long,
    /**
     * 排序专辑名称
     */
    val orderAlbumName: String,
    /**
     * 排序专辑艺术家名称
     */
    val orderAlbumArtistName: String,
    /**
     * 外部数据更新时间
     */
    val externalInfoUpdatedAt: String? = null,
    /**
     * 流派名称
     */
    val genre: String,
    /**
     * 流派信息
     */
    val genres: List<Genre>? = null,
    /**
     * 是否缺少信息
     */
    val missing: Boolean,
    /**
     * 导入时间
     */
    val importedAt: String,
    /**
     * 创建时间
     */
    val createdAt: String,
    /**
     * 更新时间
     */
    val updatedAt: String,
    /**
     * 原始日期
     */
    val originalDate: String? = null,
    /**
     * 按专辑、艺术家姓名排序
     */
    val sortAlbumArtistName: String? = null,
    val catalogNum: String? = null,

    val mbzAlbumId: String? = null,

    val mbzReleaseGroupId: String? = null,

    )