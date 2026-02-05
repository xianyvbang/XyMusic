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
    val scrobblingEnabled: String? = null,
    val adminRole: String? = null,
    val settingsRole: String? = null,
    val downloadRole: String? = null,
    val uploadRole: String? = null,
    val playlistRole: String? = null,
    val coverArtRole: String? = null,
    val commentRole: String? = null,
    val podcastRole: String? = null,
    val streamRole: String? = null,
    val jukeboxRole: String? = null,
    val shareRole: String? = null,
)
