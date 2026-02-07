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

import cn.xybbz.api.serializers.BooleanStringSerializer
import kotlinx.serialization.Serializable

@Serializable
data class SubsonicUser(
    val username: String,
    val email: String? = null,
    @Serializable(BooleanStringSerializer::class)
    val scrobblingEnabled: Boolean,
    @Serializable(BooleanStringSerializer::class)
    val adminRole: Boolean,
    @Serializable(BooleanStringSerializer::class)
    val settingsRole: Boolean,
    @Serializable(BooleanStringSerializer::class)
    val downloadRole: Boolean,
    @Serializable(BooleanStringSerializer::class)
    val uploadRole: Boolean,
    @Serializable(BooleanStringSerializer::class)
    val playlistRole: Boolean,
    @Serializable(BooleanStringSerializer::class)
    val coverArtRole: Boolean,
    @Serializable(BooleanStringSerializer::class)
    val commentRole: Boolean,
    @Serializable(BooleanStringSerializer::class)
    val podcastRole: Boolean,
    @Serializable(BooleanStringSerializer::class)
    val streamRole: Boolean,
    @Serializable(BooleanStringSerializer::class)
    val jukeboxRole: Boolean,
    @Serializable(BooleanStringSerializer::class)
    val shareRole: Boolean,
)
