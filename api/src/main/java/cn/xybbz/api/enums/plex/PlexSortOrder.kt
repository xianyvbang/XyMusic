package cn.xybbz.api.enums.plex

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class PlexSortOrder(
    private val serialName: String,
) {
    @Json(name = "asc")
    ASCENDING("asc"),

    @Json(name = "desc")
    DESCENDING("desc"), ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}