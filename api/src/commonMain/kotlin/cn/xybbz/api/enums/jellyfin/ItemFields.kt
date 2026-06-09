/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.api.enums.jellyfin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Used to control the data that gets attached to DtoBaseItems.
 */
@Serializable
enum class ItemFields(
    private val serialName: String,
) {
    @SerialName(value = "AirTime")
    AIR_TIME("AirTime"),
    @SerialName(value = "CanDelete")
    CAN_DELETE("CanDelete"),
    @SerialName(value = "CanDownload")
    CAN_DOWNLOAD("CanDownload"),
    @SerialName(value = "ChannelInfo")
    CHANNEL_INFO("ChannelInfo"),
    @SerialName(value = "Chapters")
    CHAPTERS("Chapters"),
    @SerialName(value = "Trickplay")
    TRICKPLAY("Trickplay"),
    @SerialName(value = "ChildCount")
    CHILD_COUNT("ChildCount"),
    @SerialName(value = "CumulativeRunTimeTicks")
    CUMULATIVE_RUN_TIME_TICKS("CumulativeRunTimeTicks"),
    @SerialName(value = "CustomRating")
    CUSTOM_RATING("CustomRating"),
    @SerialName(value = "DateCreated")
    DATE_CREATED("DateCreated"),
    @SerialName(value = "DateLastMediaAdded")
    DATE_LAST_MEDIA_ADDED("DateLastMediaAdded"),
    @SerialName(value = "DisplayPreferencesId")
    DISPLAY_PREFERENCES_ID("DisplayPreferencesId"),
    @SerialName(value = "Etag")
    ETAG("Etag"),
    @SerialName(value = "ExternalUrls")
    EXTERNAL_URLS("ExternalUrls"),
    @SerialName(value = "Genres")
    GENRES("Genres"),
    @SerialName(value = "HomePageUrl")
    HOME_PAGE_URL("HomePageUrl"),
    @SerialName(value = "ItemCounts")
    ITEM_COUNTS("ItemCounts"),
    @SerialName(value = "MediaSourceCount")
    MEDIA_SOURCE_COUNT("MediaSourceCount"),
    @SerialName(value = "MediaSources")
    MEDIA_SOURCES("MediaSources"),
    @SerialName(value = "OriginalTitle")
    ORIGINAL_TITLE("OriginalTitle"),
    @SerialName(value = "Overview")
    OVERVIEW("Overview"),
    @SerialName(value = "ParentId")
    PARENT_ID("ParentId"),
    @SerialName(value = "Path")
    PATH("Path"),
    @SerialName(value = "People")
    PEOPLE("People"),
    @SerialName(value = "PlayAccess")
    PLAY_ACCESS("PlayAccess"),
    @SerialName(value = "ProductionLocations")
    PRODUCTION_LOCATIONS("ProductionLocations"),
    @SerialName(value = "ProviderIds")
    PROVIDER_IDS("ProviderIds"),
    @SerialName(value = "PrimaryImageAspectRatio")
    PRIMARY_IMAGE_ASPECT_RATIO("PrimaryImageAspectRatio"),
    @SerialName(value = "RecursiveItemCount")
    RECURSIVE_ITEM_COUNT("RecursiveItemCount"),
    @SerialName(value = "Settings")
    SETTINGS("Settings"),
    @SerialName(value = "ScreenshotImageTags")
    SCREENSHOT_IMAGE_TAGS("ScreenshotImageTags"),
    @SerialName(value = "SeriesPrimaryImage")
    SERIES_PRIMARY_IMAGE("SeriesPrimaryImage"),
    @SerialName(value = "SeriesStudio")
    SERIES_STUDIO("SeriesStudio"),
    @SerialName(value = "SortName")
    SORT_NAME("SortName"),
    @SerialName(value = "SpecialEpisodeNumbers")
    SPECIAL_EPISODE_NUMBERS("SpecialEpisodeNumbers"),
    @SerialName(value = "Studios")
    STUDIOS("Studios"),
    @SerialName(value = "Taglines")
    TAGLINES("Taglines"),
    @SerialName(value = "Tags")
    TAGS("Tags"),
    @SerialName(value = "RemoteTrailers")
    REMOTE_TRAILERS("RemoteTrailers"),
    @SerialName(value = "MediaStreams")
    MEDIA_STREAMS("MediaStreams"),
    @SerialName(value = "SeasonUserData")
    SEASON_USER_DATA("SeasonUserData"),
    @SerialName(value = "ServiceName")
    SERVICE_NAME("ServiceName"),
    @SerialName(value = "ThemeSongIds")
    THEME_SONG_IDS("ThemeSongIds"),
    @SerialName(value = "ThemeVideoIds")
    THEME_VIDEO_IDS("ThemeVideoIds"),
    @SerialName(value = "ExternalEtag")
    EXTERNAL_ETAG("ExternalEtag"),
    @SerialName(value = "PresentationUniqueKey")
    PRESENTATION_UNIQUE_KEY("PresentationUniqueKey"),
    @SerialName(value = "InheritedParentalRatingValue")
    INHERITED_PARENTAL_RATING_VALUE("InheritedParentalRatingValue"),
    @SerialName(value = "ExternalSeriesId")
    EXTERNAL_SERIES_ID("ExternalSeriesId"),
    @SerialName(value = "SeriesPresentationUniqueKey")
    SERIES_PRESENTATION_UNIQUE_KEY("SeriesPresentationUniqueKey"),
    @SerialName(value = "DateLastRefreshed")
    DATE_LAST_REFRESHED("DateLastRefreshed"),
    @SerialName(value = "DateLastSaved")
    DATE_LAST_SAVED("DateLastSaved"),
    @SerialName(value = "RefreshState")
    REFRESH_STATE("RefreshState"),
    @SerialName(value = "ChannelImage")
    CHANNEL_IMAGE("ChannelImage"),
    @SerialName(value = "EnableMediaSourceDisplay")
    ENABLE_MEDIA_SOURCE_DISPLAY("EnableMediaSourceDisplay"),
    @SerialName(value = "Width")
    WIDTH("Width"),
    @SerialName(value = "Height")
    HEIGHT("Height"),
    @SerialName(value = "ExtraIds")
    EXTRA_IDS("ExtraIds"),
    @SerialName(value = "LocalTrailerCount")
    LOCAL_TRAILER_COUNT("LocalTrailerCount"),
    @SerialName(value = "IsHD")
    IS_HD("IsHD"),
    @SerialName(value = "SpecialFeatureCount")
    SPECIAL_FEATURE_COUNT("SpecialFeatureCount"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}
