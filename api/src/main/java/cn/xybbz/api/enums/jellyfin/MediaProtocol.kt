package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = false)
enum class MediaProtocol(
    private val serialName: String,
) {
    @Json(name = "File")
    FILE("File"),

    @Json(name = "Http")
    HTTP("Http"),

    @Json(name = "Rtmp")
    RTMP("Rtmp"),

    @Json(name = "Rtsp")
    RTSP("Rtsp"),

    @Json(name = "Udp")
    UDP("Udp"),

    @Json(name = "Rtp")
    RTP("Rtp"),

    @Json(name = "Ftp")
    FTP("Ftp"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}
