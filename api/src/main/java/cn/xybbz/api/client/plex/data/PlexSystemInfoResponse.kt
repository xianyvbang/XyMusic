package cn.xybbz.api.client.plex.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
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

@JsonClass(generateAdapter = true)
data class Connection(
    val protocol: String,
    val address: String,
    val port: Long,
    val uri: String,
    val local: Boolean,
    val relay: Boolean,
    val iPv6: Boolean?
)