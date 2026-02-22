package cn.xybbz.api.client.subsonic.data

import kotlinx.serialization.Serializable

@Serializable
data class ScanStatus(
    val scanning: Boolean,
    val count: Int
)