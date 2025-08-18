package cn.xybbz.api.client.plex.data

import cn.xybbz.api.client.data.AllResponse
import cn.xybbz.api.client.jellyfin.data.Response
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlexResponse<T>(
    @param:Json(name = "MediaContainer")
    val mediaContainer: T?,
    @param:Json(name = "size")
    val size: Int? = null
)