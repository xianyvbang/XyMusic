package cn.xybbz.api.client.plex.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlexLoginResponse(
    val id: String,
    val username: String,
    val authToken: String,
    val email: String,
    val uuid: String
)
