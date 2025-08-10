package cn.xybbz.api.client.plex.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlexLoginResponse(
    val id: String,
    val isAdmin: Boolean,
    val name: String,
    val subsonicSalt: String,
    val subsonicToken: String,
    val token: String,
    val username: String
)
