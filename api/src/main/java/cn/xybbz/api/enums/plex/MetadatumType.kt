package cn.xybbz.api.enums.plex

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * The type of media content in the Plex library. This can represent videos, music, or
 * photos.
 */
@JsonClass(generateAdapter = false)
enum class MetadatumType(val value: String) {
    @Json(name = "album")
    Album("album"),
    @Json(name = "artist")
    Artist("artist"),
    @Json(name = "collection")
    Collection("collection"),
    @Json(name = "episode")
    Episode("episode"),
    @Json(name = "movie")
    Movie("movie"),
    @Json(name = "photo")
    Photo("photo"),
    @Json(name = "photoalbum")
    Photoalbum("photoalbum"),
    @Json(name = "season")
    Season("season"),
    @Json(name = "show")
    Show("show"),
    @Json(name = "track")
    Track("track"),
    @Json(name = "genre")
    Genre("genre")
    ;

    override fun toString(): String {
        return value
    }
}