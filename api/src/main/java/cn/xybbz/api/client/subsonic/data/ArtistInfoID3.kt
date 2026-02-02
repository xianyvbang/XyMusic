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
 * 艺术家和相似艺术家详情
 */
@JsonClass(generateAdapter = true)
data class ArtistInfoID3(
    /**
     * 描述
     */
    val biography: String? = null,

    /**
     * A music Brainz id.
     */
    val musicBrainzId: String? = null,

    /**
     * last Fm Url
     */
    val lastFmUrl: String? = null,

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
     * 收藏时间 : 2026-01-28T02:00:15.76317727Z
     */
    val starred: String? = null,
    /**
     * 相似艺术家
     */
    val similarArtist: List<ArtistID3>? = null
)
