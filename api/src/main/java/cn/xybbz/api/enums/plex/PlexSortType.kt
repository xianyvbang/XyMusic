package cn.xybbz.api.enums.plex

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class PlexSortType(private val sortName: String) {
    /**
     * 播放次数
     */
    @Json(name = "viewCount")
    VIEWCOUNT("viewCount"),

    @Json(name = "titleSort")
    TITLE_SORT("titleSort"),


    @Json(name = "lastViewedAt")
    LAST_VIEWED_AT("lastViewedAt"),

    @Json(name = "addedAt")
    ADDED_AT("addedAt"),

    @Json(name = "updatedAt")
    UPDATED_AT("updatedAt"),

    @Json(name = "year")
    YEAR("year"),

    @Json(name = "random")
    RANDOM("random"),

    @Json(name = "artist.titleSort")
    ARTIST_TITLE_SORT("artist.titleSort"),
    @Json(name = "ratingCount")
    RATING_COUNT("ratingCount"),
    @Json(name = "album.titleSort")
    ALBUM_TITLE_SORT("album.titleSort")
    ;

    override fun toString(): String {
        return sortName
    }
}