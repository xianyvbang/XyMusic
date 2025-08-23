package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * Collection type.
 */
@JsonClass(generateAdapter = false)
enum class CollectionType(
    val serialName: String,
) {
    @Json(name = "unknown")
    UNKNOWN("unknown"),

    @Json(name = "movies")
    MOVIES("movies"),

    @Json(name = "tvshows")
    TVSHOWS("tvshows"),

    @Json(name = "music")
    MUSIC("music"),

    @Json(name = "musicvideos")
    MUSICVIDEOS("musicvideos"),

    @Json(name = "trailers")
    TRAILERS("trailers"),

    @Json(name = "homevideos")
    HOMEVIDEOS("homevideos"),

    @Json(name = "boxsets")
    BOXSETS("boxsets"),

    @Json(name = "books")
    BOOKS("books"),

    @Json(name = "photos")
    PHOTOS("photos"),

    @Json(name = "livetv")
    LIVETV("livetv"),

    @Json(name = "playlists")
    PLAYLISTS("playlists"),

    @Json(name = "folders")
    FOLDERS("folders"),

    @Json(name = "otherVideos")
    OTHER_VIDEOS("otherVideos"),

    @Json(name = "people")
    PEOPLE("people"),

    @Json(name = "tv")
    TV("tv"),

    @Json(name = "artist")
    ARTIST("artist")
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}