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

import cn.xybbz.api.enums.plex.HasThumbnail
import cn.xybbz.api.enums.plex.ImageType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlexLibraryItemResponse(

    /**
     * The Meta object is only included in the response if the `includeMeta` parameter is set to
     * `1`.
     */
    @SerialName(value = "Meta")
    val meta: Meta? = null,

    /**
     * An array of metadata items.
     */
    @SerialName(value = "Metadata")
    val metadata: List<Metadatum>? = null,
    @SerialName(value = "size")
    override val size: Int,
    @SerialName(value = "totalSize")
    override val totalSize: Int
) : PlexParentResponse()


/**
 * The Meta object is only included in the response if the `includeMeta` parameter is set to
 * `1`.
 */
@Serializable
data class Meta(
    @SerialName(value = "FieldType")
    val fieldType: List<FieldType>? = null,

    @SerialName(value = "Type")
    val type: List<Type>? = null
)

@Serializable
data class FieldType(
    @SerialName(value = "Operator")
    val operator: List<Operator>,

    val type: String
)

@Serializable
data class Operator(
    val key: String,
    val title: String
)

@Serializable
data class Type(
    val active: Boolean,

    @SerialName(value = "Field")
    val field: List<Field>? = null,

    @SerialName(value = "Filter")
    val filter: List<Filter>? = null,

    val key: String,

    val title: String,
    val type: String
)

@Serializable
data class Field(
    val key: String,
    val subType: String? = null,
    val title: String,
    val type: String
)

@Serializable
data class Filter(
    val filter: String,
    val filterType: String,
    val key: String,
    val title: String,
    val type: String
)


/**
 * The identifier for the chapter
 *
 * The filter for the chapter
 *
 * The index for the chapter
 *
 * The start time offset for the chapter
 *
 * The end time offset for the chapter
 *
 * The thumbnail for the chapter
 */
@Serializable
data class Chapter(
    val id: Long,
    val filter: String,
    val index: Long,
    val startTimeOffset: Long,
    val endTimeOffset: Long,
    val thumb: String
)

@Serializable
data class Collection(
    /**
     * The user-made collection this media item belongs to
     */
    val tag: String
)

@Serializable
data class Country(
    /**
     * The unique identifier for the country.
     * NOTE: This is different for each Plex server and is not globally unique.
     */
    val id: Long? = null,

    /**
     * The country of origin of this media item
     */
    val tag: String
)

@Serializable
data class Director(
    /**
     * Unique identifier for the director.
     */
    val id: Long,

    /**
     * The role of Director
     */
    val tag: String,

    /**
     * The absolute URL of the thumbnail image for the director.
     */
    val thumb: String? = null
)

@Serializable
data class Extras(
    /**
     * The size of the extras.
     */
    val size: Long? = null
)

@Serializable
data class Genre(

    /**
     * The genre name of this media-item
     */
    val tag: String
)

@Serializable
data class GUID(
    /**
     * The unique identifier for the Guid. Can be prefixed with imdb://, tmdb://, tvdb://
     */
    val id: String
)

@Serializable
data class Image(
    val alt: String,
    val type: ImageType,
    val url: String
)


/**
 * The folder path for the media item.
 */
@Serializable
data class Location(
    val path: String
)

/**
 * The identifier for the marker
 *
 * The type of the marker
 *
 * The start time offset for the marker
 *
 * The end time offset for the marker
 *
 * The final status of the marker
 */
@Serializable
data class Marker(
    val id: Long,
    val type: String,
    val startTimeOffset: Long,
    val endTimeOffset: Long,
    val final: Boolean? = null,

    /**
     * Attributes associated with the marker.
     */
    @SerialName(value = "Attributes")
    val attributes: Attributes? = null
)

/**
 * Attributes associated with the marker.
 */
@Serializable
data class Attributes(
    /**
     * The identifier for the attributes.
     */
    val id: Long,

    /**
     * The version number of the marker attributes.
     */
    val version: Long? = null
)

@Serializable
data class Media(
    /**
     * Unique media identifier.
     */
    val id: Long,

    /**
     * Duration of the media in milliseconds.
     */
    val duration: Long? = null,

    /**
     * Bitrate in bits per second.
     */
    val bitrate: Int? = null,

    /**
     * Video width in pixels.
     */
    val width: Long? = null,

    /**
     * Video height in pixels.
     */
    val height: Long? = null,

    /**
     * Aspect ratio of the video.
     */
    val aspectRatio: Double? = null,

    /**
     * Number of audio channels.
     */
    val audioChannels: Long? = null,

    val displayOffset: Long? = null,

    /**
     * Audio codec used.
     */
    val audioCodec: String? = null,

    /**
     * Video codec used.
     */
    val videoCodec: String? = null,

    /**
     * Video resolution (e.g., 4k).
     */
    val videoResolution: String? = null,

    /**
     * Container format of the media.
     */
    val container: String? = null,

    /**
     * Frame rate of the video. Values found include NTSC, PAL, 24p
     */
    val videoFrameRate: String? = null,

    /**
     * Video profile (e.g., main 10).
     */
    val videoProfile: String? = null,

    /**
     * Indicates whether voice activity is detected.
     */
    val hasVoiceActivity: Boolean? = null,

    /**
     * The audio profile used for the media (e.g., DTS, Dolby Digital, etc.).
     */
    val audioProfile: String? = null,

    /**
     * Has this media been optimized for streaming. NOTE: This can be 0, 1, false or true
     */
    val optimizedForStreaming: String? = null,

    /**
     * Indicates whether the media has 64-bit offsets.
     * This is relevant for media files that may require larger offsets than what 32-bit
     * integers can provide.
     */
    @SerialName(value = "has64bitOffsets")
    val has64BitOffsets: Boolean? = null,

    @SerialName(value = "Part")
    val part: List<Part>? = null
)

@Serializable
data class Part(
    /**
     * Indicates if the part is accessible.
     */
    val accessible: Boolean? = null,

    /**
     * Indicates if the part exists.
     */
    val exists: Boolean? = null,

    /**
     * Unique part identifier.
     */
    val id: Long,

    /**
     * Key to access this part.
     */
    val key: String? = null,

    val indexes: String? = null,

    /**
     * Duration of the part in milliseconds.
     */
    val duration: Long? = null,

    /**
     * File path for the part.
     */
    val file: String? = null,

    /**
     * File size in bytes.
     */
    val size: Long? = null,

    val packetLength: Long? = null,

    /**
     * Container format of the part.
     */
    val container: String? = null,

    /**
     * Video profile for the part.
     */
    val videoProfile: String? = null,

    /**
     * The audio profile used for the media (e.g., DTS, Dolby Digital, etc.).
     */
    val audioProfile: String? = null,

    @SerialName(value = "has64bitOffsets")
    val has64BitOffsets: Boolean? = null,

    /**
     * Has this media been optimized for streaming. NOTE: This can be 0, 1, false or true
     */
    val optimizedForStreaming: String? = null,

    val hasThumbnail: HasThumbnail? = null,

    val stream: List<Stream>? = null
)

@Serializable
data class Stream(
    /**
     * Unique stream identifier.
     */
    val id: Long,

    /**
     * Stream type:
     * - VIDEO = 1
     * - AUDIO = 2
     * - SUBTITLE = 3
     */
    val streamType: Long,

    /**
     * Format of the stream (e.g., srt).
     */
    val format: String? = null,

    /**
     * Indicates if this stream is default.
     */
    val default: Boolean? = null,

    /**
     * Codec used by the stream.
     */
    val codec: String? = null,

    /**
     * Index of the stream.
     */
    val index: Long? = null,

    /**
     * Bitrate of the stream.
     */
    val bitrate: Int? = null,

    /**
     * Language of the stream.
     */
    val language: String? = null,

    /**
     * Language tag (e.g., en).
     */
    val languageTag: String? = null,

    /**
     * ISO language code.
     */
    val languageCode: String? = null,

    /**
     * Indicates whether header compression is enabled.
     */
    val headerCompression: Boolean? = null,

    /**
     * Dolby Vision BL compatibility ID.
     */
    @SerialName(value = "DOVIBLCompatID")
    val doviblCompatID: Long? = null,

    /**
     * Indicates if Dolby Vision BL is present.
     */
    @SerialName(value = "DOVIBLPresent")
    val doviblPresent: Boolean? = null,

    /**
     * Indicates if Dolby Vision EL is present.
     */
    @SerialName(value = "DOVIELPresent")
    val dovielPresent: Boolean? = null,

    /**
     * Dolby Vision level.
     */
    @SerialName(value = "DOVILevel")
    val doviLevel: Long? = null,

    /**
     * Indicates if Dolby Vision is present.
     */
    @SerialName(value = "DOVIPresent")
    val doviPresent: Boolean? = null,

    /**
     * Dolby Vision profile.
     */
    @SerialName(value = "DOVIProfile")
    val doviProfile: Long? = null,

    /**
     * Indicates if Dolby Vision RPU is present.
     */
    @SerialName(value = "DOVIRPUPresent")
    val dovirpuPresent: Boolean? = null,

    /**
     * Dolby Vision version.
     */
    @SerialName(value = "DOVIVersion")
    val doviVersion: String? = null,

    /**
     * Bit depth of the video stream.
     */
    val bitDepth: Int? = null,

    /**
     * Chroma sample location.
     */
    val chromaLocation: String? = null,

    /**
     * Chroma subsampling format.
     */
    val chromaSubsampling: String? = null,

    /**
     * Coded video height.
     */
    val codedHeight: Long? = null,

    /**
     * Coded video width.
     */
    val codedWidth: Long? = null,

    val closedCaptions: Boolean? = null,

    /**
     * Color primaries used.
     */
    val colorPrimaries: String? = null,

    /**
     * Color range (e.g., tv).
     */
    val colorRange: String? = null,

    /**
     * Color space.
     */
    val colorSpace: String? = null,

    /**
     * Color transfer characteristics.
     */
    val colorTrc: String? = null,

    /**
     * Frame rate of the stream.
     */
    val frameRate: Double? = null,

    /**
     * Key to access this stream part.
     */
    val key: String? = null,

    /**
     * Height of the video stream.
     */
    val height: Long? = null,

    /**
     * Video level.
     */
    val level: Long? = null,

    /**
     * Indicates if this is the original stream.
     */
    val original: Boolean? = null,

    val hasScalingMatrix: Boolean? = null,

    /**
     * Video profile.
     */
    val profile: String? = null,

    val scanType: String? = null,
    val embeddedInVideo: String? = null,

    /**
     * Number of reference frames.
     */
    val refFrames: Long? = null,

    /**
     * Width of the video stream.
     */
    val width: Long? = null,

    /**
     * Display title for the stream.
     */
    val displayTitle: String? = null,

    /**
     * Extended display title for the stream.
     */
    val extendedDisplayTitle: String? = null,

    /**
     * Indicates if this stream is selected (applicable for audio streams).
     */
    val selected: Boolean? = null,

    val forced: Boolean? = null,

    /**
     * Number of audio channels (for audio streams).
     */
    val channels: Long? = null,

    /**
     * Audio channel layout.
     */
    val audioChannelLayout: String? = null,

    /**
     * Sampling rate for the audio stream.
     */
    val samplingRate: Int? = null,

    /**
     * Indicates if the stream can auto-sync.
     */
    val canAutoSync: Boolean? = null,

    /**
     * Indicates if the stream is for the hearing impaired.
     */
    val hearingImpaired: Boolean? = null,

    /**
     * Indicates if the stream is a dub.
     */
    val dub: Boolean? = null,

    /**
     * Optional title for the stream (e.g., language variant).
     */
    val title: String? = null
)

@Serializable
data class Producer(
    /**
     * The filter string for the role.
     */
    val filter: String,

    /**
     * The unique role identifier.
     */
    val id: Long,

    /**
     * The character name or role.
     */
    val role: String? = null,

    /**
     * The actor's name.
     */
    val tag: String,

    /**
     * A key associated with the actor tag.
     */
    val tagKey: String,

    /**
     * URL for the role thumbnail image.
     */
    val thumb: String? = null
)

@Serializable
data class Rating(
    /**
     * The image or reference for the rating.
     */
    val image: String,

    /**
     * The type of rating (e.g., audience, critic).
     */
    val type: String,

    /**
     * The rating value.
     */
    val value: Double
)

@Serializable
data class Role(
    /**
     * The unique identifier for the role.
     * NOTE: This is different for each Plex server and is not globally unique.
     */
    val id: Long,

    /**
     * The display tag for the actor (typically the actor's name).
     */
    val tag: String,

    /**
     * The role played by the actor in the media item.
     */
    val role: String? = null,

    /**
     * The absolute URL of the thumbnail image for the actor.
     */
    val thumb: String? = null
)

/**
 * Setting that indicates the episode ordering for the show.
 * Options:
 * - None = Library default
 * - tmdbAiring = The Movie Database (Aired)
 * - aired = TheTVDB (Aired)
 * - dvd = TheTVDB (DVD)
 * - absolute = TheTVDB (Absolute)
 */
enum class ShowOrdering(val value: String) {
    Absolute("absolute"),
    Aired("aired"),
    DVD("dvd"),
    None("None"),
    TmdbAiring("tmdbAiring");

    override fun toString(): String {
        return value
    }
}

@Serializable
data class Similar(
    /**
     * The filter string for similar items.
     */
    val filter: String,

    /**
     * The unique similar item identifier.
     */
    val id: Long,

    /**
     * The tag or title of the similar content.
     */
    val tag: String
)

@Serializable

data class UltraBlurColors(
    val bottomLeft: String,
    val bottomRight: String,
    val topLeft: String,
    val topRight: String
)

@Serializable
data class Writer(
    /**
     * Unique identifier for the writer.
     */
    val id: Long,

    /**
     * The role of Writer
     */
    val tag: String,

    /**
     * The absolute URL of the thumbnail image for the writer.
     */
    val thumb: String? = null
)