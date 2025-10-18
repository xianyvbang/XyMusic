package cn.xybbz.api.enums.plex

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Indicates if the part has a thumbnail.
 */
@JsonClass(generateAdapter = false)
enum class HasThumbnail(val serialName: String) {
    @Json(name = "0")
    The0("0"),
    @Json(name = "1")
    The1("1");

    override fun toString(): String = serialName
}