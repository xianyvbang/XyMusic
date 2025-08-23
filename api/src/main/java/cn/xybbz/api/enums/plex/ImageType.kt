package cn.xybbz.api.enums.plex

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = false)
enum class ImageType(val value: String) {
    @Json(name = "background")
    Background("background"),
    @Json(name = "clearLogo")
    ClearLogo("clearLogo"),
    @Json(name = "coverPoster")
    CoverPoster("coverPoster"),
    @Json(name = "snapshot")
    Snapshot("snapshot");

    override fun toString(): String {
        return value
    }
}