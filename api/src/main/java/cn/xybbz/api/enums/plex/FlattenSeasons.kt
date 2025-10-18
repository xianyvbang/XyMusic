package cn.xybbz.api.enums.plex

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * Setting that indicates if seasons are set to hidden for the show. (-1 = Library default,
 * 0 = Hide, 1 = Show).
 */
@JsonClass(generateAdapter = false)
enum class FlattenSeasons(val value: String) {
    @Json(name = "1")
    FlattenSeasons1("1"),
    @Json(name = "0")
    The0("0"),
    @Json(name = "-1")
    The1("-1");

    override fun toString(): String {
        return value
    }
}
