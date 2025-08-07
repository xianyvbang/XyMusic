package cn.xybbz.api.client.jellyfin.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionsPlayingRequest(
    val itemId:String,
    val playSessionId: String,
    val positionTicks: Long,
    val isPaused: Boolean,
    val playbackRate: Double,
    val playMethod: String = "DirectPlay"
)
