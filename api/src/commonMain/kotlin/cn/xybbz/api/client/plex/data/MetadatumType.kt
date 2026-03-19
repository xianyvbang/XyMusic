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

package cn.xybbz.api.client.plex.data

import cn.xybbz.api.enums.plex.FlattenSeasons
import cn.xybbz.api.enums.plex.MetadatumType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The available images for this media item
 *
 *
 * Unknown
 */
@Serializable
data class Metadatum(
    val type: MetadatumType,

    /**
     * The rating key (Media ID) of this media item. Note: Although this is always an integer,
     * it is represented as a string in the API.
     */
    val ratingKey: String,

    /**
     * The unique key for the media item.
     */
    val key: String,

    /**
     * The globally unique identifier for the media item.
     */
    @SerialName(value = "guid")
    val guid: String,

    /**
     * A URL‐friendly version of the media title.
     */
    val slug: String? = null,

    /**
     * The studio that produced the media item.
     */
    val studio: String? = null,

    /**
     * The title of the media item.
     */
    val title: String,

    /**
     * The banner image URL for the media item.
     */
    val banner: String? = null,

    /**
     * The sort title used for ordering media items.
     */
    val titleSort: String? = null,

    /**
     * The content rating for the media item.
     */
    val contentRating: String? = null,

    /**
     * A synopsis of the media item.
     */
    val summary: String,

    /**
     * The critic rating for the media item.
     */
    @SerialName(value = "rating")
    val metadatumRating: Double? = null,

    /**
     * The audience rating for the media item.
     */
    val audienceRating: Double? = null,

    /**
     * The release year of the media item.
     */
    val year: Int? = null,

    /**
     * A brief tagline for the media item.
     */
    val tagline: String? = null,

    /**
     * The thumbnail image URL for the media item.
     */
    val thumb: String? = null,

    /**
     * The art image URL for the media item.
     */
    val art: String? = null,

    /**
     * The theme URL for the media item.
     */
    val theme: String? = null,

    /**
     * The index position of the media item.
     */
    val index: Int? = null,

    /**
     * The number of leaf items (end nodes) under this media item.
     */
    val leafCount: Long? = null,

    /**
     * The number of leaf items that have been viewed.
     */
    val viewedLeafCount: Long? = null,

    /**
     * The number of child items associated with this media item.
     */
    val childCount: Long? = null,

    /**
     * The total number of seasons (for TV shows).
     */
    val seasonCount: Long? = null,

    /**
     * The duration of the media item in milliseconds.
     */
    val duration: Long? = null,

    /**
     * The original release date of the media item.
     */
    val originallyAvailableAt: LocalDate? = null,

    val addedAt: Long,

    /**
     * Unix epoch datetime in seconds
     */
    val updatedAt: Long? = null,

    /**
     * The URL for the audience rating image.
     */
    val audienceRatingImage: String? = null,

    /**
     * The source from which chapter data is derived.
     */
    val chapterSource: String? = null,

    /**
     * The primary extra key associated with this media item.
     */
    val primaryExtraKey: String? = null,

    /**
     * The original title of the media item (if different).
     */
    val originalTitle: String? = null,

    /**
     * The rating key of the parent media item.
     */
    val parentRatingKey: String? = null,

    /**
     * The rating key of the grandparent media item.
     */
    val grandparentRatingKey: String? = null,

    /**
     * The GUID of the parent media item.
     */
    @SerialName(value = "parentGuid")
    val parentGUID: String? = null,

    /**
     * The GUID of the grandparent media item.
     */
    @SerialName(value = "grandparentGuid")
    val grandparentGUID: String? = null,

    /**
     * The slug for the grandparent media item.
     */
    val grandparentSlug: String? = null,

    /**
     * The key of the grandparent media item.
     */
    val grandparentKey: String? = null,

    /**
     * The key of the parent media item.
     */
    val parentKey: String? = null,

    /**
     * The title of the grandparent media item.
     */
    val grandparentTitle: String? = null,

    /**
     * The thumbnail URL for the grandparent media item.
     */
    val grandparentThumb: String? = null,

    /**
     * The theme URL for the grandparent media item.
     */
    val grandparentTheme: String? = null,

    /**
     * The art URL for the grandparent media item.
     */
    val grandparentArt: String? = null,

    /**
     * The title of the parent media item.
     */
    val parentTitle: String? = null,

    /**
     * The index position of the parent media item.
     */
    val parentIndex: Long? = null,

    val parentYear: Int? = null,

    /**
     * The thumbnail URL for the parent media item.
     */
    val parentThumb: String? = null,

    /**
     * The URL for the rating image.
     */
    val ratingImage: String? = null,

    /**
     * The number of times this media item has been viewed.
     */
    val viewCount: Long? = null,

    /**
     * The current playback offset (in milliseconds).
     */
    val viewOffset: Long? = null,

    /**
     * The number of times this media item has been skipped.
     */
    val skipCount: Long? = null,

    /**
     * A classification that further describes the type of media item. For example, 'clip'
     * indicates that the item is a short video clip.
     */
    val subtype: String? = null,

    /**
     * The Unix timestamp representing the last time the item was rated.
     */
    val lastRatedAt: Long? = null,

    /**
     * The accuracy of the creation timestamp. This value indicates the format(s) provided (for
     * example, 'epoch,local' means both epoch and local time formats are available).
     */
    val createdAtAccuracy: String? = null,

    /**
     * The time zone offset for the creation timestamp, represented as a string. This offset
     * indicates the difference from UTC.
     */
    val createdAtTZOffset: String? = null,

    /**
     * Unix timestamp for when the media item was last viewed.
     */
    val lastViewedAt: Long? = null,

    /**
     * The rating provided by a user for the item. This value is expressed as a decimal number.
     */
    val userRating: Double? = null,

    @SerialName(value = "Image")
    val image: List<Image>? = null,

    @SerialName(value = "UltraBlurColors")
    val ultraBlurColors: UltraBlurColors? = null,

    @SerialName(value = "Guid")
    val guidList: List<GUID>? = null,

    /**
     * The identifier for the library section.
     */
    val librarySectionID: Long? = null,

    /**
     * The title of the library section.
     */
    val librarySectionTitle: String? = null,

    /**
     * The key corresponding to the library section.
     */
    val librarySectionKey: String? = null,

    /**
     * Setting that indicates the episode ordering for the show.
     * Options:
     * - None = Library default
     * - tmdbAiring = The Movie Database (Aired)
     * - aired = TheTVDB (Aired)
     * - dvd = TheTVDB (DVD)
     * - absolute = TheTVDB (Absolute)
     */
    val showOrdering: ShowOrdering? = null,

    /**
     * Setting that indicates if seasons are set to hidden for the show. (-1 = Library default,
     * 0 = Hide, 1 = Show).
     */
    val flattenSeasons: FlattenSeasons? = null,

    /**
     * Indicates whether child items should be skipped.
     */
    val skipChildren: Boolean? = null,

    @SerialName(value = "Chapter")
    val chapter: List<Chapter>? = null,

    @SerialName(value = "Collection")
    val collection: List<Collection>? = null,

    @SerialName(value = "Country")
    val country: List<Country>? = null,

    @SerialName(value = "Director")
    val director: List<Director>? = null,

    @SerialName(value = "Extras")
    val extras: Extras? = null,

    @SerialName(value = "Genre")
    val genre: List<Genre>? = null,

    @SerialName(value = "Location")
    val location: List<Location>? = null,

    @SerialName(value = "Marker")
    val marker: List<Marker>? = null,

    @SerialName(value = "Media")
    val media: List<Media>? = null,

    @SerialName(value = "Producer")
    val producer: List<Producer>? = null,

    @SerialName(value = "Rating")
    val rating: List<Rating>? = null,

    @SerialName(value = "Role")
    val role: List<Role>? = null,

    @SerialName(value = "Similar")
    val similar: List<Similar>? = null,

    @SerialName(value = "Writer")
    val writer: List<Writer>? = null,

    @SerialName(value = "playlistItemID")
    val playlistItemID: String? = null
)
