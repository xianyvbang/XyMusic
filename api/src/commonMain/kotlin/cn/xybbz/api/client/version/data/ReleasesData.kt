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

package cn.xybbz.api.client.version.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ReleasesData(
    @SerialName(value = "tag_name")
    val tagName: String,
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "body")
    val body: String,
    @SerialName(value = "prerelease")
    val prerelease: Boolean,
    @SerialName(value = "published_at")
    val publishedAt: String,
    @SerialName(value = "assets")
    val assets: List<AssetItem>,
    val message: String? = null,
    @SerialName("documentation_url")
    val documentationUrl: String? = null

)