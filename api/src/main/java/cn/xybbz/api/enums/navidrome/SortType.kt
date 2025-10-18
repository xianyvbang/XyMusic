package cn.xybbz.api.enums.navidrome

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class SortType(private val sortName: String) {
    @Json(name = "id")
    ID("id"),

    @Json(name = "updatedAt")
    UPDATED_AT("updatedAt"),

    @Json(name = "createdAt")
    CREATED_AT("createdAt"),

    @Json(name = "name")
    NAME("name"),

    @Json(name = "title")
    TITLE("title"),

    @Json(name = "sync")
    SYNC("sync"),

    @Json(name = "starred_at")
    STARRED_AT("starred_at"),

    /**
     * 最近添加
     */
    @Json(name = "recently_added")
    RECENTLY_ADDED("recently_added"),

    /**
     * 播放时间
     */
    @Json(name = "play_date")
    PLAY_DATE("play_date"),

    /**
     * 播放次数
     */
    @Json(name = "play_count")
    PLAY_COUNT("play_count"),

    @Json(name = "albumArtist")
    ALBUM_ARTIST("albumArtist"),

    @Json(name = "max_year")
    MAX_YEAR("max_year"),

    @Json(name = "album")
    ALBUM("album"),

    @Json(name = "artist")
    ARTIST("artist"),

    @Json(name = "year")
    YEAR("year"),

    @Json(name = "random")
    RANDOM("random")
    ;

    override fun toString(): String {
        return sortName
    }
}