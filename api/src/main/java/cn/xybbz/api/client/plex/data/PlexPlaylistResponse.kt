package cn.xybbz.api.client.plex.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * plex歌单信息
 */
@JsonClass(generateAdapter = true)
data class PlexPlaylistResponse(
   @param:Json(name = "Metadata")
   val metadata: List<PlaylistMetadatum>?
): PlexParentResponse()


@JsonClass(generateAdapter = true)
data class PlaylistMetadatum (
   val addedAt: Long? = null,
   val composite: String? = null,
   val duration: Long? = null,
   val guid: String? = null,
   val icon: String? = null,
   val key: String,
   val lastViewedAt: Long? = null,
   val leafCount: Long? = null,
   val playlistType: String? = null,
   val ratingKey: String,
   val smart: Boolean? = null,
   val summary: String? = null,
   val title: String? = null,
   val type: String? = null,
   val updatedAt: Long? = null,
   val viewCount: Long? = null
)