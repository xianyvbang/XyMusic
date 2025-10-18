package cn.xybbz.api.client.navidrome.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NavidromeCreatePlaylistResponse(
    val id: String? = ""
)
