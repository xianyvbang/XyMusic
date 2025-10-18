package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * These represent sort orders.
 */
@JsonClass(generateAdapter = false)
enum class ItemSortBy(
    private val serialName: String,
) {
    @Json(name = "Default")
    DEFAULT("Default"),

    @Json(name = "AiredEpisodeOrder")
    AIRED_EPISODE_ORDER("AiredEpisodeOrder"),

    @Json(name = "Album")
    ALBUM("Album"),

    @Json(name = "AlbumArtist")
    ALBUM_ARTIST("AlbumArtist"),

    @Json(name = "Artist")
    ARTIST("Artist"),

    @Json(name = "DateCreated")
    DATE_CREATED("DateCreated"),

    @Json(name = "OfficialRating")
    OFFICIAL_RATING("OfficialRating"),

    @Json(name = "DatePlayed")
    DATE_PLAYED("DatePlayed"),

    @Json(name = "PremiereDate")
    PREMIERE_DATE("PremiereDate"),

    @Json(name = "StartDate")
    START_DATE("StartDate"),

    @Json(name = "SortName")
    SORT_NAME("SortName"),

    @Json(name = "Name")
    NAME("Name"),

    @Json(name = "Random")
    RANDOM("Random"),

    @Json(name = "Runtime")
    RUNTIME("Runtime"),

    @Json(name = "CommunityRating")
    COMMUNITY_RATING("CommunityRating"),

    @Json(name = "ProductionYear")
    PRODUCTION_YEAR("ProductionYear"),

    @Json(name = "PlayCount")
    PLAY_COUNT("PlayCount"),

    @Json(name = "CriticRating")
    CRITIC_RATING("CriticRating"),

    @Json(name = "IsFolder")
    IS_FOLDER("IsFolder"),

    @Json(name = "IsUnplayed")
    IS_UNPLAYED("IsUnplayed"),

    @Json(name = "IsPlayed")
    IS_PLAYED("IsPlayed"),

    @Json(name = "SeriesSortName")
    SERIES_SORT_NAME("SeriesSortName"),

    @Json(name = "VideoBitRate")
    VIDEO_BIT_RATE("VideoBitRate"),

    @Json(name = "AirTime")
    AIR_TIME("AirTime"),

    @Json(name = "Studio")
    STUDIO("Studio"),

    @Json(name = "IsFavoriteOrLiked")
    IS_FAVORITE_OR_LIKED("IsFavoriteOrLiked"),

    @Json(name = "DateLastContentAdded")
    DATE_LAST_CONTENT_ADDED("DateLastContentAdded"),

    @Json(name = "SeriesDatePlayed")
    SERIES_DATE_PLAYED("SeriesDatePlayed"),

    @Json(name = "ParentIndexNumber")
    PARENT_INDEX_NUMBER("ParentIndexNumber"),

    @Json(name = "IndexNumber")
    INDEX_NUMBER("IndexNumber"),

    @Json(name = "SimilarityScore")
    SIMILARITY_SCORE("SimilarityScore"),

    @Json(name = "SearchScore")
    SEARCH_SCORE("SearchScore"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}