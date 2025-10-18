package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * Enum MediaStreamType.
 */
@JsonClass(generateAdapter = false)
enum class MediaStreamType(
    val serialName: String,
) {
    @Json(name = "Audio")
    AUDIO("Audio"),

    @Json(name = "Video")
    VIDEO("Video"),

    @Json(name = "Subtitle")
    SUBTITLE("Subtitle"),

    @Json(name = "EmbeddedImage")
    EMBEDDED_IMAGE("EmbeddedImage"),

    @Json(name = "Data")
    DATA("Data"),

    @Json(name = "Lyric")
    LYRIC("Lyric"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName

}