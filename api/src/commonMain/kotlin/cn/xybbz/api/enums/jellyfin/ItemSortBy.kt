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
 * These represent sort orders.
 */
@Serializable
enum class ItemSortBy(
    private val serialName: String,
) {
    @SerialName(value = "Default")
    DEFAULT("Default"),

    @SerialName(value = "AiredEpisodeOrder")
    AIRED_EPISODE_ORDER("AiredEpisodeOrder"),

    @SerialName(value = "Album")
    ALBUM("Album"),

    @SerialName(value = "AlbumArtist")
    ALBUM_ARTIST("AlbumArtist"),

    @SerialName(value = "Artist")
    ARTIST("Artist"),

    @SerialName(value = "DateCreated")
    DATE_CREATED("DateCreated"),

    @SerialName(value = "OfficialRating")
    OFFICIAL_RATING("OfficialRating"),

    @SerialName(value = "DatePlayed")
    DATE_PLAYED("DatePlayed"),

    @SerialName(value = "PremiereDate")
    PREMIERE_DATE("PremiereDate"),

    @SerialName(value = "StartDate")
    START_DATE("StartDate"),

    @SerialName(value = "SortName")
    SORT_NAME("SortName"),

    @SerialName(value = "Name")
    NAME("Name"),

    @SerialName(value = "Random")
    RANDOM("Random"),

    @SerialName(value = "Runtime")
    RUNTIME("Runtime"),

    @SerialName(value = "CommunityRating")
    COMMUNITY_RATING("CommunityRating"),

    @SerialName(value = "ProductionYear")
    PRODUCTION_YEAR("ProductionYear"),

    @SerialName(value = "PlayCount")
    PLAY_COUNT("PlayCount"),

    @SerialName(value = "CriticRating")
    CRITIC_RATING("CriticRating"),

    @SerialName(value = "IsFolder")
    IS_FOLDER("IsFolder"),

    @SerialName(value = "IsUnplayed")
    IS_UNPLAYED("IsUnplayed"),

    @SerialName(value = "IsPlayed")
    IS_PLAYED("IsPlayed"),

    @SerialName(value = "SeriesSortName")
    SERIES_SORT_NAME("SeriesSortName"),

    @SerialName(value = "VideoBitRate")
    VIDEO_BIT_RATE("VideoBitRate"),

    @SerialName(value = "AirTime")
    AIR_TIME("AirTime"),

    @SerialName(value = "Studio")
    STUDIO("Studio"),

    @SerialName(value = "IsFavoriteOrLiked")
    IS_FAVORITE_OR_LIKED("IsFavoriteOrLiked"),

    @SerialName(value = "DateLastContentAdded")
    DATE_LAST_CONTENT_ADDED("DateLastContentAdded"),

    @SerialName(value = "SeriesDatePlayed")
    SERIES_DATE_PLAYED("SeriesDatePlayed"),

    @SerialName(value = "ParentIndexNumber")
    PARENT_INDEX_NUMBER("ParentIndexNumber"),

    @SerialName(value = "IndexNumber")
    INDEX_NUMBER("IndexNumber"),

    @SerialName(value = "SimilarityScore")
    SIMILARITY_SCORE("SimilarityScore"),

    @SerialName(value = "SearchScore")
    SEARCH_SCORE("SearchScore"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}