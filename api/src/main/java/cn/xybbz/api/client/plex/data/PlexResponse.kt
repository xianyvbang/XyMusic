package cn.xybbz.api.client.plex.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlexResponse<T>(
    @param:Json(name = "MediaContainer")
    val mediaContainer: T?,
    @param:Json(name = "size")
    val size: Int? = null
)