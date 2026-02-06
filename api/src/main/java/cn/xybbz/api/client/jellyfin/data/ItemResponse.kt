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

package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.enums.jellyfin.CollectionType
import cn.xybbz.api.enums.jellyfin.ImageType
import cn.xybbz.api.enums.jellyfin.MediaProtocol
import cn.xybbz.api.enums.jellyfin.MediaSourceType
import cn.xybbz.api.enums.jellyfin.MediaStreamType
import cn.xybbz.api.serializers.LocalDateTimeTimestampSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * 数据响应
 * @author Administrator
 * @date 2025/12/02
 * @constructor 创建[ItemResponse]
 */
@Serializable
data class ItemResponse(
    /**
     * The id.
     */
    @SerialName(value = "Id")
    val id: String,
    /**
     * The name.
     */
    @SerialName(value = "Name")
    val name: String? = null,
    /**
     * 专辑图片标签
     */
    @SerialName(value = "AlbumPrimaryImageTag")
    val albumPrimaryImageTag: String? = null,
    /**
     * The album artists.
     */
    @SerialName(value = "AlbumArtists")
    val albumArtists: List<NameGuidPair>? = null,

    /**
     * The production year.
     */
    @SerialName(value = "ProductionYear")
    val productionYear: Int? = null,

    /**
     * The premiere date.
     */
    @SerialName(value = "PremiereDate")
    val premiereDate: String? = null,

    /**
     * The album id.
     */
    @SerialName(value = "AlbumId")
    val albumId: String? = null,

    /**
     * The media versions.
     */
    @SerialName(value = "MediaSources")
    val mediaSources: List<MediaSourceInfo>? = null,

    /**
     * The genres.
     */
    @SerialName(value = "Genres")
    val genres: List<String>? = null,

    @SerialName(value = "GenreItems")
    val genreItems: List<NameGuidPair>? = null,

    /**
     * The album.
     */
    @SerialName(value = "Album")
    val album: String? = null,

    /**
     * The artist items.
     */
    @SerialName(value = "ArtistItems")
    val artistItems: List<NameGuidPair>? = null,

    /**
     * The date created.
     */
    @SerialName(value = "DateCreated")
    @Serializable(LocalDateTimeTimestampSerializer::class)
    val dateCreated: Long,

    /**
     * The user data for this item based on the user it's being requested for.
     */
    @SerialName(value = "UserData")
    val userData: UserItemDataDto? = null,

    /**
     * The type of the collection.
     */
    @SerialName(value = "CollectionType")
    val collectionType: CollectionType? = null,

    /**
     * The image tags.
     */
    @SerialName(value = "ImageTags")
    val imageTags: Map<ImageType, String>? = null,

    /**
     * backdrop image tags
     */
    @SerialName(value = "BackdropImageTags")
    val backdropImageTags: List<String>? = null,

    /**
     * The name of the sort.
     */
    @SerialName(value = "SortName")
    val sortName: String? = null,

    /**
     * The overview.
     */
    @SerialName(value = "Overview")
    val overview: String? = null,

    /**
     * The song count.
     */
    @SerialName(value = "SongCount")
    val songCount: Int? = null,

    /**
     * The album count.
     */
    @SerialName(value = "AlbumCount")
    val albumCount: Int? = null,

    @SerialName(value = "ChildCount")
    val childCount: Int? = null,
)


/**
 * Class UserItemDataDto.
 */
@Serializable
data class UserItemDataDto(
    /**
     * The rating.
     */
    @SerialName(value = "Rating")
    val rating: Double? = null,
    /**
     * The played percentage.
     */
    @SerialName(value = "PlayedPercentage")
    val playedPercentage: Double? = null,
    /**
     * The unplayed item count.
     */
    @SerialName(value = "UnplayedItemCount")
    val unplayedItemCount: Int? = null,
    /**
     * The playback position ticks.
     */
    @SerialName(value = "PlaybackPositionTicks")
    val playbackPositionTicks: Long,
    /**
     * The play count.
     */
    @SerialName(value = "PlayCount")
    val playCount: Int,
    /**
     * A value indicating whether this instance is favorite.
     */
    @SerialName(value = "IsFavorite")
    val isFavorite: Boolean,
    /**
     * A value indicating whether this MediaBrowser.Model.Dto.UserItemDataDto is likes.
     */
    @SerialName(value = "Likes")
    val likes: Boolean? = null,
    /**
     * The last played date.
     */
    @SerialName(value = "LastPlayedDate")
    @Serializable(LocalDateTimeTimestampSerializer::class)
    val lastPlayedDate: Long? = null,
    /**
     * A value indicating whether this MediaBrowser.Model.Dto.UserItemDataDto is played.
     */
    @SerialName(value = "Played")
    val played: Boolean,
    /**
     * The key.
     */
    @SerialName(value = "Key")
    val key: String? = null,
    /**
     * The item identifier.
     */
    @SerialName(value = "ItemId")
    val itemId: String? = null,
)


@Serializable
data class MediaSourceInfo(
    @SerialName(value = "Protocol")
    val protocol: MediaProtocol,
    @SerialName(value = "Id")
    val id: String? = null,
    @SerialName(value = "Path")
    val path: String,
    @SerialName(value = "EncoderPath")
    val encoderPath: String? = null,
    @SerialName(value = "EncoderProtocol")
    val encoderProtocol: MediaProtocol? = null,
    @SerialName(value = "Type")
    val type: MediaSourceType,
    @SerialName(value = "Container")
    val container: String? = null,
    @SerialName(value = "Size")
    val size: Long? = null,
    @SerialName(value = "Name")
    val name: String? = null,
    @SerialName(value = "RunTimeTicks")
    val runTimeTicks: Long? = null,
    @SerialName(value = "MediaStreams")
    val mediaStreams: List<MediaStream>? = null,
    @SerialName(value = "Bitrate")
    val bitrate: Int? = null
)

/**
 * Class MediaStream.
 */
@Serializable
data class MediaStream(
    /**
     * The codec.
     */
    @SerialName(value = "Codec")
    val codec: String? = null,
    /**
     * The codec tag.
     */
    @SerialName(value = "CodecTag")
    val codecTag: String? = null,
    /**
     * The language.
     */
    @SerialName(value = "Language")
    val language: String? = null,
    /**
     * The color range.
     */
    @SerialName(value = "ColorRange")
    val colorRange: String? = null,
    /**
     * The color space.
     */
    @SerialName(value = "ColorSpace")
    val colorSpace: String? = null,
    /**
     * The color transfer.
     */
    @SerialName(value = "ColorTransfer")
    val colorTransfer: String? = null,
    /**
     * The color primaries.
     */
    @SerialName(value = "ColorPrimaries")
    val colorPrimaries: String? = null,
    /**
     * The Dolby Vision version major.
     */
    @SerialName(value = "DvVersionMajor")
    val dvVersionMajor: Int? = null,
    /**
     * The Dolby Vision version minor.
     */
    @SerialName(value = "DvVersionMinor")
    val dvVersionMinor: Int? = null,
    /**
     * The Dolby Vision profile.
     */
    @SerialName(value = "DvProfile")
    val dvProfile: Int? = null,
    /**
     * The Dolby Vision level.
     */
    @SerialName(value = "DvLevel")
    val dvLevel: Int? = null,
    /**
     * The Dolby Vision rpu present flag.
     */
    @SerialName(value = "RpuPresentFlag")
    val rpuPresentFlag: Int? = null,
    /**
     * The Dolby Vision el present flag.
     */
    @SerialName(value = "ElPresentFlag")
    val elPresentFlag: Int? = null,
    /**
     * The Dolby Vision bl present flag.
     */
    @SerialName(value = "BlPresentFlag")
    val blPresentFlag: Int? = null,
    /**
     * The Dolby Vision bl signal compatibility id.
     */
    @SerialName(value = "DvBlSignalCompatibilityId")
    val dvBlSignalCompatibilityId: Int? = null,
    /**
     * The Rotation in degrees.
     */
    @SerialName(value = "Rotation")
    val rotation: Int? = null,
    /**
     * The comment.
     */
    @SerialName(value = "Comment")
    val comment: String? = null,
    /**
     * The time base.
     */
    @SerialName(value = "TimeBase")
    val timeBase: String? = null,
    /**
     * The codec time base.
     */
    @SerialName(value = "CodecTimeBase")
    val codecTimeBase: String? = null,
    /**
     * The title.
     */
    @SerialName(value = "Title")
    val title: String? = null,
    /**
     * The video dovi title.
     */
    @SerialName(value = "VideoDoViTitle")
    val videoDoViTitle: String? = null,
    @SerialName(value = "LocalizedUndefined")
    val localizedUndefined: String? = null,
    @SerialName(value = "LocalizedDefault")
    val localizedDefault: String? = null,
    @SerialName(value = "LocalizedForced")
    val localizedForced: String? = null,
    @SerialName(value = "LocalizedExternal")
    val localizedExternal: String? = null,
    @SerialName(value = "LocalizedHearingImpaired")
    val localizedHearingImpaired: String? = null,
    @SerialName(value = "DisplayTitle")
    val displayTitle: String? = null,
    @SerialName(value = "NalLengthSize")
    val nalLengthSize: String? = null,
    /**
     * A value indicating whether this instance is interlaced.
     */
    @SerialName(value = "IsInterlaced")
    val isInterlaced: Boolean,
    @SerialName(value = "IsAVC")
    val isAvc: Boolean? = null,
    /**
     * The channel layout.
     */
    @SerialName(value = "ChannelLayout")
    val channelLayout: String? = null,
    /**
     * The bit rate.
     */
    @SerialName(value = "BitRate")
    val bitRate: Int? = null,
    /**
     * The bit depth.
     */
    @SerialName(value = "BitDepth")
    val bitDepth: Int? = null,
    /**
     * The reference frames.
     */
    @SerialName(value = "RefFrames")
    val refFrames: Int? = null,
    /**
     * The length of the packet.
     */
    @SerialName(value = "PacketLength")
    val packetLength: Int? = null,
    /**
     * The channels.
     */
    @SerialName(value = "Channels")
    val channels: Int? = null,
    /**
     * The sample rate.
     */
    @SerialName(value = "SampleRate")
    val sampleRate: Int? = null,
    /**
     * A value indicating whether this instance is default.
     */
    @SerialName(value = "IsDefault")
    val isDefault: Boolean,
    /**
     * A value indicating whether this instance is forced.
     */
    @SerialName(value = "IsForced")
    val isForced: Boolean,
    /**
     * A value indicating whether this instance is for the hearing impaired.
     */
    @SerialName(value = "IsHearingImpaired")
    val isHearingImpaired: Boolean? = null,
    /**
     * The height.
     */
    @SerialName(value = "Height")
    val height: Int? = null,
    /**
     * The width.
     */
    @SerialName(value = "Width")
    val width: Int? = null,
    /**
     * The average frame rate.
     */
    @SerialName(value = "AverageFrameRate")
    val averageFrameRate: Float? = null,
    /**
     * The real frame rate.
     */
    @SerialName(value = "RealFrameRate")
    val realFrameRate: Float? = null,
    /**
     * Gets the framerate used as reference.
     * Prefer AverageFrameRate, if that is null or an unrealistic value
     * then fallback to RealFrameRate.
     */
    @SerialName(value = "ReferenceFrameRate")
    val referenceFrameRate: Float? = null,
    /**
     * The profile.
     */
    @SerialName(value = "Profile")
    val profile: String? = null,
    /**
     * The type.
     */
    @SerialName(value = "Type")
    val type: MediaStreamType,
    /**
     * The aspect ratio.
     */
    @SerialName(value = "AspectRatio")
    val aspectRatio: String? = null,
    /**
     * The index.
     */
    @SerialName(value = "Index")
    val index: Int,
    /**
     * The score.
     */
    @SerialName(value = "Score")
    val score: Int? = null,
    /**
     * A value indicating whether this instance is external.
     */
    @SerialName(value = "IsExternal")
    val isExternal: Boolean,
    /**
     * The delivery URL.
     */
    @SerialName(value = "DeliveryUrl")
    val deliveryUrl: String? = null,
    /**
     * A value indicating whether this instance is external URL.
     */
    @SerialName(value = "IsExternalUrl")
    val isExternalUrl: Boolean? = null,
    @SerialName(value = "IsTextSubtitleStream")
    val isTextSubtitleStream: Boolean,
    /**
     * A value indicating whether [supports external stream].
     */
    @SerialName(value = "SupportsExternalStream")
    val supportsExternalStream: Boolean,
    /**
     * The filename.
     */
    @SerialName(value = "Path")
    val path: String? = null,
    /**
     * The pixel format.
     */
    @SerialName(value = "PixelFormat")
    val pixelFormat: String? = null,
)
