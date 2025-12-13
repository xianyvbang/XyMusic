package cn.xybbz.api.client.jellyfin.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NameGuidPair(
    @param:Json(name = "Name") val name: String? = null,
    @param:Json(name = "Id") val id: String,
)