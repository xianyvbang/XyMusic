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

package cn.xybbz.api.client.plex.data

import cn.xybbz.api.enums.plex.MetadatumType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlexLibrary(
    @param:Json(name = "Directory")
    val directory: List<Directory>? = null,
    @param:Json(name = "size")
    override val size: Int,
    @param:Json(name = "totalSize")
    override val totalSize: Int? = null
) : PlexParentResponse(size, totalSize = totalSize)

@JsonClass(generateAdapter = true)
data class Directory(
    val title: String,
    val type: MetadatumType? = null,
    /**
     * 是否可以下载
     */
    val allowSync:Boolean?,
    val uuid: String? = null,
    val key: String,
    val createdAt: Long? = null
)