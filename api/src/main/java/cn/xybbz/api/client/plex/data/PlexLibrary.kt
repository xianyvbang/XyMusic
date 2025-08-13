package cn.xybbz.api.client.plex.data

import cn.xybbz.api.enums.jellyfin.CollectionType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlexLibrary(
    @param:Json(name = "Directory")
    val directory: List<Directory>,
    @param:Json(name = "size")
    override val size:Int
):PlexParentResponse(size)

@JsonClass(generateAdapter = true)
data class Directory(
    val title: String,
    val type: CollectionType,
    val uuid: String,
    val key: String,
    val createdAt: Long
)