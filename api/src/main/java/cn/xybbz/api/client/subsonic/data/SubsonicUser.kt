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

@JsonClass(generateAdapter = true)
data class SubsonicUser(
    val username: String,
    val email: String? = null,
    val scrobblingEnabled: Boolean? = null,
    val adminRole: Boolean? = null,
    val settingsRole: Boolean? = null,
    val downloadRole: Boolean? = null,
    val uploadRole: Boolean? = null,
    val playlistRole: Boolean? = null,
    val coverArtRole: Boolean? = null,
    val commentRole: Boolean? = null,
    val podcastRole: Boolean? = null,
    val streamRole: Boolean? = null,
    val jukeboxRole: Boolean? = null,
    val shareRole: Boolean? = null,
)
