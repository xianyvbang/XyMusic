package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = false)
enum class MediaSourceType(
    private val serialName: String,
) {
    @Json(name = "Default")
    DEFAULT("Default"),

    @Json(name = "Grouping")
    GROUPING("Grouping"),

    @Json(name = "Placeholder")
    PLACEHOLDER("Placeholder"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName

}