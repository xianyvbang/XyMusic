package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class PlayMethod(
    val serialName: String,
) {
    @Json(name = "Transcode")
    TRANSCODE("Transcode"),

    @Json(name = "DirectStream")
    DIRECT_STREAM("DirectStream"),

    @Json(name = "DirectPlay")
    DIRECT_PLAY("DirectPlay"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}
