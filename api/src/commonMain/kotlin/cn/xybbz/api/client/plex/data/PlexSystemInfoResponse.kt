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

import kotlinx.serialization.Serializable

@Serializable
data class PlexSystemInfoResponse(
    val name: String,
    val product: String,
    val productVersion: String,
    val platform: String? = null,
    val platformVersion: String? = null,
    val device: String? = null,
    val clientIdentifier: String,
    val createdAt: String,
    val lastSeenAt: String,
    val provides: String,
    val ownerID: String? = null,
    val sourceTitle: String? = null,
    val publicAddress: String,
    val accessToken: String,
    val owned: Boolean,
    val home: Boolean,
    val synced: Boolean,
    val relay: Boolean,
    val presence: Boolean,
    val httpsRequired: Boolean,
    val publicAddressMatches: Boolean,
    val dnsRebindingProtection: Boolean,
    val natLoopbackSupported: Boolean,
    val connections: List<Connection>
)

@Serializable
data class Connection(
    val protocol: String,
    val address: String,
    val port: Long,
    val uri: String,
    val local: Boolean,
    val relay: Boolean,
    val iPv6: Boolean?
)