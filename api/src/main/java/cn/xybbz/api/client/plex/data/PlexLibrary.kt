package cn.xybbz.api.client.plex.data

import cn.xybbz.api.enums.jellyfin.CollectionType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlexLibrary(
    @param:Json(name = "MediaContainer")
    val mediaContainer: MediaContainer
)

@JsonClass(generateAdapter = true)
data class MediaContainer(
    @param:Json(name = "Directory")
    val directory: List<Directory>
)

@JsonClass(generateAdapter = true)
data class Directory(
    val title: String,
    val type: CollectionType,
    val uuid: String,
    val createdAt: Long
)