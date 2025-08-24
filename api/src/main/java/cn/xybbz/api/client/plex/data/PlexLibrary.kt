package cn.xybbz.api.client.plex.data

import cn.xybbz.api.enums.plex.MetadatumType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlexLibrary(
    @param:Json(name = "Directory")
    val directory: List<Directory>,
    @param:Json(name = "size")
    override val size: Int
) : PlexParentResponse(size)

@JsonClass(generateAdapter = true)
data class Directory(
    val title: String,
    val type: MetadatumType? = null,
    val uuid: String?= null,
    val key: String,
    val createdAt: Long? = null
)