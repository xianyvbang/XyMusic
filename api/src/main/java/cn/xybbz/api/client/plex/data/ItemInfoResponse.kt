package cn.xybbz.api.client.plex.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemInfoResponse(
    @param:Json(name = "Metadata")
    val metadata: List<Metadatum>? = null,
)
