package cn.xybbz.api.enums.plex

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class PlexPlaylistType(val serialName: String) {

    @Json(name = "audio")
    AUDIO("audio")

    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}