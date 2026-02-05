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

data class NavidromeUser(
    val id: String? = null,
    val userName: String? = null,
    val name: String? = null,
    val email: String? = null,
    val isAdmin: Boolean? = null,
    val lastLoginAt: String? = null,
    val lastAccessAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val libraries: List<Libraries>? = null,
)

data class Libraries(
    val id: Int? = null,
    val name: String? = null,
    val path: String? = null,
    val remotePath: String? = null,
    val lastScanAt: String? = null,
    val lastScanStartedAt: String? = null,
    val fullScanInProgress: Boolean? = null,
    val updatedAt: String? = null,
    val createdAt: String? = null,
    val totalSongs: Int? = null,
    val totalAlbums: Int? = null,
    val totalArtists: Int? = null,
    val totalFolders: Int? = null,
    val totalFiles: Int? = null,
    val totalMissingFiles: Int? = null,
    val totalSize: Int? = null,
    val totalDuration: Int? = null,
    val defaultNewUsers: Boolean? = null,
)
