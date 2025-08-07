package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * An enum representing the sorting order.
 */
@JsonClass(generateAdapter = false)
enum class SortOrder(
    private val serialName: String,
) {
    @Json(name = "Ascending")
    ASCENDING("Ascending"),

    @Json(name = "Descending")
    DESCENDING("Descending"), ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}