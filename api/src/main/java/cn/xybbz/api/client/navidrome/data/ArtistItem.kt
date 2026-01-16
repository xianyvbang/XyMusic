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
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class ArtistItem(
    val id: String,
    val name: String,
    /**
     * 平分
     */
    val rating: Int? = null,
    /**
     * 是否收藏
     */
    val starred: Boolean? = null,
    /**
     * 收藏时间
     */
    val starredAt: LocalDateTime? = null,
    /**
     * 所属歌曲文件大小,单位字节
     */
    val size: Long,
    /**
     * 专辑数量
     */
    val albumCount: Long? = null,
    /**
     * 音乐数量
     */
    val songCount: Long? = null,
    /**
     * 播放次数
     */
    val playCount: Long? = null,
    /**
     * 播放时间
     */
    val playDate: LocalDateTime? = null,
    /**
     * 小图片链接地址
     */
    val smallImageUrl: String? = null,
    /**
     * 中等图像链接地址
     */
    val mediumImageUrl: String? = null,
    /**
     * 大图片链接
     */
    val largeImageUrl: String? = null,
    /**
     * 外部信息更新时间
     */
    val externalInfoUpdatedAt: LocalDateTime? = null,
    /**
     * 是否数据缺失
     */
    val missing: Boolean,
    /**
     * 创建时间
     */
    val createdAt: LocalDateTime,
    /**
     * 更新时间
     */
    val updatedAt: LocalDateTime,
    /**
     * 按艺术家姓名排序
     */
    val sortArtistName: String? = null,
    /**
     * 艺术家排序名称
     */
    val orderArtistName: String ? = null,
    /**
     * mbz艺术家id
     */
    val mbzArtistId: String? = null
)
