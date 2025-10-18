package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * Media streaming protocol.
 * Lowercase for backwards compatibility.
 */
@JsonClass(generateAdapter = false)
enum class MediaStreamProtocol(
    private val serialName: String,
) {
    @Json(name = "http")
    HTTP("http"),

    @Json(name = "hls")
    HLS("hls"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}