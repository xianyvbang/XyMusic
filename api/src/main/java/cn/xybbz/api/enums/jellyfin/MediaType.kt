package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * Media types.
 */
@JsonClass(generateAdapter = false)
enum class MediaType(
    val serialName: String,
) {
    @Json(name = "Unknown")
    UNKNOWN("Unknown"),

    @Json(name = "Video")
    VIDEO("Video"),

    @Json(name = "Audio")
    AUDIO("Audio"),

    @Json(name = "Photo")
    PHOTO("Photo"),

    @Json(name = "Book")
    BOOK("Book"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}