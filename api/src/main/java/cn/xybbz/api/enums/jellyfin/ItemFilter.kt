package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Enum ItemFilter.
 */
@JsonClass(generateAdapter = false)
enum class ItemFilter(
    private val serialName: String,
) {
    @Json(name = "IsFolder")
    IS_FOLDER("IsFolder"),

    @Json(name = "IsNotFolder")
    IS_NOT_FOLDER("IsNotFolder"),

    @Json(name = "IsUnplayed")
    IS_UNPLAYED("IsUnplayed"),

    @Json(name = "IsPlayed")
    IS_PLAYED("IsPlayed"),

    @Json(name = "IsFavorite")
    IS_FAVORITE("IsFavorite"),

    @Json(name = "IsResumable")
    IS_RESUMABLE("IsResumable"),

    @Json(name = "Likes")
    LIKES("Likes"),

    @Json(name = "Dislikes")
    DISLIKES("Dislikes"),

    @Json(name = "IsFavoriteOrLikes")
    IS_FAVORITE_OR_LIKES("IsFavoriteOrLikes"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}