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

import cn.xybbz.api.client.data.Request
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(value = "SubsonicSearchRequest")
data class SubsonicSearchRequest(
    /**
     * Search query.
     */
    val query:String,
    /**
     * Maximum number of artists to return.
     */
    val artistCount: Int = 20,
    /**
     * Search result offset for artists. Used for paging.
     */
    val artistOffset: Int = 0,
    /**
     * Maximum number of albums to return.
     */
    val albumCount: Int = 20,
    /**
     * Search result offset for albums. Used for paging.
     */
    val albumOffset: Int = 0,
    /**
     * Maximum number of songs to return.
     */
    val songCount: Int = 20,
    /**
     * Search result offset for songs. Used for paging.
     */
    val songOffset: Int = 0,
    /**
     * 	(Since 1.12.0) Only return results from music folder with the given ID. See getMusicFolders.
     */
    val musicFolderId: String? = null
) : Request()