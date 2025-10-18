package cn.xybbz.api.enums.plex

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class PlayState(val serialName: String) {

    @Json(name = "playing")
    PLAYING("playing"),

    @Json(name = "paused")
    PAUSED("paused")
    ;

    override fun toString(): String = serialName
}