package cn.xybbz.api.client.plex.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class PlexParentResponse(open val size: Int? = null, open val totalSize: Int? = null)