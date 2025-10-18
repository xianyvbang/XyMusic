package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * Used to control the data that gets attached to DtoBaseItems.
 */
@JsonClass(generateAdapter = false)
enum class ItemFields(
    private val serialName: String,
) {
    @Json(name = "AirTime")
    AIR_TIME("AirTime"),
    @Json(name = "CanDelete")
    CAN_DELETE("CanDelete"),
    @Json(name = "CanDownload")
    CAN_DOWNLOAD("CanDownload"),
    @Json(name = "ChannelInfo")
    CHANNEL_INFO("ChannelInfo"),
    @Json(name = "Chapters")
    CHAPTERS("Chapters"),
    @Json(name = "Trickplay")
    TRICKPLAY("Trickplay"),
    @Json(name = "ChildCount")
    CHILD_COUNT("ChildCount"),
    @Json(name = "CumulativeRunTimeTicks")
    CUMULATIVE_RUN_TIME_TICKS("CumulativeRunTimeTicks"),
    @Json(name = "CustomRating")
    CUSTOM_RATING("CustomRating"),
    @Json(name = "DateCreated")
    DATE_CREATED("DateCreated"),
    @Json(name = "DateLastMediaAdded")
    DATE_LAST_MEDIA_ADDED("DateLastMediaAdded"),
    @Json(name = "DisplayPreferencesId")
    DISPLAY_PREFERENCES_ID("DisplayPreferencesId"),
    @Json(name = "Etag")
    ETAG("Etag"),
    @Json(name = "ExternalUrls")
    EXTERNAL_URLS("ExternalUrls"),
    @Json(name = "Genres")
    GENRES("Genres"),
    @Json(name = "HomePageUrl")
    HOME_PAGE_URL("HomePageUrl"),
    @Json(name = "ItemCounts")
    ITEM_COUNTS("ItemCounts"),
    @Json(name = "MediaSourceCount")
    MEDIA_SOURCE_COUNT("MediaSourceCount"),
    @Json(name = "MediaSources")
    MEDIA_SOURCES("MediaSources"),
    @Json(name = "OriginalTitle")
    ORIGINAL_TITLE("OriginalTitle"),
    @Json(name = "Overview")
    OVERVIEW("Overview"),
    @Json(name = "ParentId")
    PARENT_ID("ParentId"),
    @Json(name = "Path")
    PATH("Path"),
    @Json(name = "People")
    PEOPLE("People"),
    @Json(name = "PlayAccess")
    PLAY_ACCESS("PlayAccess"),
    @Json(name = "ProductionLocations")
    PRODUCTION_LOCATIONS("ProductionLocations"),
    @Json(name = "ProviderIds")
    PROVIDER_IDS("ProviderIds"),
    @Json(name = "PrimaryImageAspectRatio")
    PRIMARY_IMAGE_ASPECT_RATIO("PrimaryImageAspectRatio"),
    @Json(name = "RecursiveItemCount")
    RECURSIVE_ITEM_COUNT("RecursiveItemCount"),
    @Json(name = "Settings")
    SETTINGS("Settings"),
    @Json(name = "ScreenshotImageTags")
    SCREENSHOT_IMAGE_TAGS("ScreenshotImageTags"),
    @Json(name = "SeriesPrimaryImage")
    SERIES_PRIMARY_IMAGE("SeriesPrimaryImage"),
    @Json(name = "SeriesStudio")
    SERIES_STUDIO("SeriesStudio"),
    @Json(name = "SortName")
    SORT_NAME("SortName"),
    @Json(name = "SpecialEpisodeNumbers")
    SPECIAL_EPISODE_NUMBERS("SpecialEpisodeNumbers"),
    @Json(name = "Studios")
    STUDIOS("Studios"),
    @Json(name = "Taglines")
    TAGLINES("Taglines"),
    @Json(name = "Tags")
    TAGS("Tags"),
    @Json(name = "RemoteTrailers")
    REMOTE_TRAILERS("RemoteTrailers"),
    @Json(name = "MediaStreams")
    MEDIA_STREAMS("MediaStreams"),
    @Json(name = "SeasonUserData")
    SEASON_USER_DATA("SeasonUserData"),
    @Json(name = "ServiceName")
    SERVICE_NAME("ServiceName"),
    @Json(name = "ThemeSongIds")
    THEME_SONG_IDS("ThemeSongIds"),
    @Json(name = "ThemeVideoIds")
    THEME_VIDEO_IDS("ThemeVideoIds"),
    @Json(name = "ExternalEtag")
    EXTERNAL_ETAG("ExternalEtag"),
    @Json(name = "PresentationUniqueKey")
    PRESENTATION_UNIQUE_KEY("PresentationUniqueKey"),
    @Json(name = "InheritedParentalRatingValue")
    INHERITED_PARENTAL_RATING_VALUE("InheritedParentalRatingValue"),
    @Json(name = "ExternalSeriesId")
    EXTERNAL_SERIES_ID("ExternalSeriesId"),
    @Json(name = "SeriesPresentationUniqueKey")
    SERIES_PRESENTATION_UNIQUE_KEY("SeriesPresentationUniqueKey"),
    @Json(name = "DateLastRefreshed")
    DATE_LAST_REFRESHED("DateLastRefreshed"),
    @Json(name = "DateLastSaved")
    DATE_LAST_SAVED("DateLastSaved"),
    @Json(name = "RefreshState")
    REFRESH_STATE("RefreshState"),
    @Json(name = "ChannelImage")
    CHANNEL_IMAGE("ChannelImage"),
    @Json(name = "EnableMediaSourceDisplay")
    ENABLE_MEDIA_SOURCE_DISPLAY("EnableMediaSourceDisplay"),
    @Json(name = "Width")
    WIDTH("Width"),
    @Json(name = "Height")
    HEIGHT("Height"),
    @Json(name = "ExtraIds")
    EXTRA_IDS("ExtraIds"),
    @Json(name = "LocalTrailerCount")
    LOCAL_TRAILER_COUNT("LocalTrailerCount"),
    @Json(name = "IsHD")
    IS_HD("IsHD"),
    @Json(name = "SpecialFeatureCount")
    SPECIAL_FEATURE_COUNT("SpecialFeatureCount"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}
