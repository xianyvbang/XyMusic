package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.client.jellyfin.data.NameGuidPair
import cn.xybbz.api.enums.jellyfin.CollectionType
import cn.xybbz.api.enums.jellyfin.ImageType
import cn.xybbz.api.enums.jellyfin.MediaProtocol
import cn.xybbz.api.enums.jellyfin.MediaSourceType
import cn.xybbz.api.enums.jellyfin.MediaStreamType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime


/**
 * 数据响应
 * @author Administrator
 * @date 2025/12/02
 * @constructor 创建[ItemResponse]
 */
@JsonClass(generateAdapter = true)
data class ItemResponse(
    /**
     * The id.
     */
    @param:Json(name = "Id")
    val id: String,
    /**
     * The name.
     */
    @param:Json(name = "Name")
    val name: String? = null,
    /**
     * 专辑图片标签
     */
    @param:Json(name = "AlbumPrimaryImageTag")
    val albumPrimaryImageTag: String? = null,
    /**
     * The album artists.
     */
    @param:Json(name = "AlbumArtists")
    val albumArtists: List<NameGuidPair>? = null,

    /**
     * The production year.
     */
    @param:Json(name = "ProductionYear")
    val productionYear: Int? = null,

    /**
     * The premiere date.
     */
    @param:Json(name = "PremiereDate")
    val premiereDate: LocalDateTime? = null,

    /**
     * The album id.
     */
    @param:Json(name = "AlbumId")
    val albumId: String? = null,

    /**
     * The media versions.
     */
    @param:Json(name = "MediaSources")
    val mediaSources: List<MediaSourceInfo>? = null,

    /**
     * The genres.
     */
    @param:Json(name = "Genres")
    val genres: List<String>? = null,

    @param:Json(name = "GenreItems")
    val genreItems:List<NameGuidPair>? = null,

    /**
     * The album.
     */
    @param:Json(name = "Album")
    val album: String? = null,

    /**
     * The artist items.
     */
    @param:Json(name = "ArtistItems")
    val artistItems: List<NameGuidPair>? = null,

    /**
     * The date created.
     */
    @param:Json(name = "DateCreated")
    val dateCreated: LocalDateTime? = null,

    /**
     * The user data for this item based on the user it's being requested for.
     */
    @param:Json(name = "UserData")
    val userData: UserItemDataDto? = null,

    /**
     * The type of the collection.
     */
    @param:Json(name = "CollectionType")
    val collectionType: CollectionType? = null,

    /**
     * The image tags.
     */
    @param:Json(name = "ImageTags")
    val imageTags: Map<ImageType, String>? = null,

    /**
     * backdrop image tags
     */
    @param:Json(name = "BackdropImageTags")
    val backdropImageTags: List<String>? = null,

    /**
     * The name of the sort.
     */
    @param:Json(name = "SortName")
    val sortName: String? = null,

    /**
     * The overview.
     */
    @param:Json(name = "Overview")
    val overview: String? = null,

    /**
     * The song count.
     */
    @param:Json(name = "SongCount")
    val songCount: Int? = null,

    /**
     * The album count.
     */
    @param:Json(name = "AlbumCount")
    val albumCount: Int? = null,
)


/**
 * Class UserItemDataDto.
 */
@JsonClass(generateAdapter = true)
data class UserItemDataDto(
    /**
     * The rating.
     */
    @param:Json(name = "Rating")
    val rating: Double? = null,
    /**
     * The played percentage.
     */
    @param:Json(name = "PlayedPercentage")
    val playedPercentage: Double? = null,
    /**
     * The unplayed item count.
     */
    @param:Json(name = "UnplayedItemCount")
    val unplayedItemCount: Int? = null,
    /**
     * The playback position ticks.
     */
    @param:Json(name = "PlaybackPositionTicks")
    val playbackPositionTicks: Long,
    /**
     * The play count.
     */
    @param:Json(name = "PlayCount")
    val playCount: Int,
    /**
     * A value indicating whether this instance is favorite.
     */
    @param:Json(name = "IsFavorite")
    val isFavorite: Boolean,
    /**
     * A value indicating whether this MediaBrowser.Model.Dto.UserItemDataDto is likes.
     */
    @param:Json(name = "Likes")
    val likes: Boolean? = null,
    /**
     * The last played date.
     */
    @param:Json(name = "LastPlayedDate")
    val lastPlayedDate: LocalDateTime? = null,
    /**
     * A value indicating whether this MediaBrowser.Model.Dto.UserItemDataDto is played.
     */
    @param:Json(name = "Played")
    val played: Boolean,
    /**
     * The key.
     */
    @param:Json(name = "Key")
    val key: String? = null,
    /**
     * The item identifier.
     */
    @param:Json(name = "ItemId")
    val itemId: String? = null,
)


@JsonClass(generateAdapter = true)
data class MediaSourceInfo(
    @param:Json(name = "Protocol")
    val protocol: MediaProtocol,
    @param:Json(name = "Id")
    val id: String? = null,
    @param:Json(name = "Path")
    val path: String,
    @param:Json(name = "EncoderPath")
    val encoderPath: String? = null,
    @param:Json(name = "EncoderProtocol")
    val encoderProtocol: MediaProtocol? = null,
    @param:Json(name = "Type")
    val type: MediaSourceType,
    @param:Json(name = "Container")
    val container: String? = null,
    @param:Json(name = "Size")
    val size: Long? = null,
    @param:Json(name = "Name")
    val name: String? = null,
    @param:Json(name = "RunTimeTicks")
    val runTimeTicks: Long? = null,
    @param:Json(name = "MediaStreams")
    val mediaStreams: List<MediaStream>? = null,
    @param:Json(name = "Bitrate")
    val bitrate: Int? = null
)

/**
 * Class MediaStream.
 */
@JsonClass(generateAdapter = true)
data class MediaStream(
    /**
     * The codec.
     */
    @param:Json(name = "Codec")
    val codec: String? = null,
    /**
     * The codec tag.
     */
    @param:Json(name = "CodecTag")
    val codecTag: String? = null,
    /**
     * The language.
     */
    @param:Json(name = "Language")
    val language: String? = null,
    /**
     * The color range.
     */
    @param:Json(name = "ColorRange")
    val colorRange: String? = null,
    /**
     * The color space.
     */
    @param:Json(name = "ColorSpace")
    val colorSpace: String? = null,
    /**
     * The color transfer.
     */
    @param:Json(name = "ColorTransfer")
    val colorTransfer: String? = null,
    /**
     * The color primaries.
     */
    @param:Json(name = "ColorPrimaries")
    val colorPrimaries: String? = null,
    /**
     * The Dolby Vision version major.
     */
    @param:Json(name = "DvVersionMajor")
    val dvVersionMajor: Int? = null,
    /**
     * The Dolby Vision version minor.
     */
    @param:Json(name = "DvVersionMinor")
    val dvVersionMinor: Int? = null,
    /**
     * The Dolby Vision profile.
     */
    @param:Json(name = "DvProfile")
    val dvProfile: Int? = null,
    /**
     * The Dolby Vision level.
     */
    @param:Json(name = "DvLevel")
    val dvLevel: Int? = null,
    /**
     * The Dolby Vision rpu present flag.
     */
    @param:Json(name = "RpuPresentFlag")
    val rpuPresentFlag: Int? = null,
    /**
     * The Dolby Vision el present flag.
     */
    @param:Json(name = "ElPresentFlag")
    val elPresentFlag: Int? = null,
    /**
     * The Dolby Vision bl present flag.
     */
    @param:Json(name = "BlPresentFlag")
    val blPresentFlag: Int? = null,
    /**
     * The Dolby Vision bl signal compatibility id.
     */
    @param:Json(name = "DvBlSignalCompatibilityId")
    val dvBlSignalCompatibilityId: Int? = null,
    /**
     * The Rotation in degrees.
     */
    @param:Json(name = "Rotation")
    val rotation: Int? = null,
    /**
     * The comment.
     */
    @param:Json(name = "Comment")
    val comment: String? = null,
    /**
     * The time base.
     */
    @param:Json(name = "TimeBase")
    val timeBase: String? = null,
    /**
     * The codec time base.
     */
    @param:Json(name = "CodecTimeBase")
    val codecTimeBase: String? = null,
    /**
     * The title.
     */
    @param:Json(name = "Title")
    val title: String? = null,
    /**
     * The video dovi title.
     */
    @param:Json(name = "VideoDoViTitle")
    val videoDoViTitle: String? = null,
    @param:Json(name = "LocalizedUndefined")
    val localizedUndefined: String? = null,
    @param:Json(name = "LocalizedDefault")
    val localizedDefault: String? = null,
    @param:Json(name = "LocalizedForced")
    val localizedForced: String? = null,
    @param:Json(name = "LocalizedExternal")
    val localizedExternal: String? = null,
    @param:Json(name = "LocalizedHearingImpaired")
    val localizedHearingImpaired: String? = null,
    @param:Json(name = "DisplayTitle")
    val displayTitle: String? = null,
    @param:Json(name = "NalLengthSize")
    val nalLengthSize: String? = null,
    /**
     * A value indicating whether this instance is interlaced.
     */
    @param:Json(name = "IsInterlaced")
    val isInterlaced: Boolean,
    @param:Json(name = "IsAVC")
    val isAvc: Boolean? = null,
    /**
     * The channel layout.
     */
    @param:Json(name = "ChannelLayout")
    val channelLayout: String? = null,
    /**
     * The bit rate.
     */
    @param:Json(name = "BitRate")
    val bitRate: Int? = null,
    /**
     * The bit depth.
     */
    @param:Json(name = "BitDepth")
    val bitDepth: Int? = null,
    /**
     * The reference frames.
     */
    @param:Json(name = "RefFrames")
    val refFrames: Int? = null,
    /**
     * The length of the packet.
     */
    @param:Json(name = "PacketLength")
    val packetLength: Int? = null,
    /**
     * The channels.
     */
    @param:Json(name = "Channels")
    val channels: Int? = null,
    /**
     * The sample rate.
     */
    @param:Json(name = "SampleRate")
    val sampleRate: Int? = null,
    /**
     * A value indicating whether this instance is default.
     */
    @param:Json(name = "IsDefault")
    val isDefault: Boolean,
    /**
     * A value indicating whether this instance is forced.
     */
    @param:Json(name = "IsForced")
    val isForced: Boolean,
    /**
     * A value indicating whether this instance is for the hearing impaired.
     */
    @param:Json(name = "IsHearingImpaired")
    val isHearingImpaired: Boolean? = null,
    /**
     * The height.
     */
    @param:Json(name = "Height")
    val height: Int? = null,
    /**
     * The width.
     */
    @param:Json(name = "Width")
    val width: Int? = null,
    /**
     * The average frame rate.
     */
    @param:Json(name = "AverageFrameRate")
    val averageFrameRate: Float? = null,
    /**
     * The real frame rate.
     */
    @param:Json(name = "RealFrameRate")
    val realFrameRate: Float? = null,
    /**
     * Gets the framerate used as reference.
     * Prefer AverageFrameRate, if that is null or an unrealistic value
     * then fallback to RealFrameRate.
     */
    @param:Json(name = "ReferenceFrameRate")
    val referenceFrameRate: Float? = null,
    /**
     * The profile.
     */
    @param:Json(name = "Profile")
    val profile: String? = null,
    /**
     * The type.
     */
    @param:Json(name = "Type")
    val type: MediaStreamType,
    /**
     * The aspect ratio.
     */
    @param:Json(name = "AspectRatio")
    val aspectRatio: String? = null,
    /**
     * The index.
     */
    @param:Json(name = "Index")
    val index: Int,
    /**
     * The score.
     */
    @param:Json(name = "Score")
    val score: Int? = null,
    /**
     * A value indicating whether this instance is external.
     */
    @param:Json(name = "IsExternal")
    val isExternal: Boolean,
    /**
     * The delivery URL.
     */
    @param:Json(name = "DeliveryUrl")
    val deliveryUrl: String? = null,
    /**
     * A value indicating whether this instance is external URL.
     */
    @param:Json(name = "IsExternalUrl")
    val isExternalUrl: Boolean? = null,
    @param:Json(name = "IsTextSubtitleStream")
    val isTextSubtitleStream: Boolean,
    /**
     * A value indicating whether [supports external stream].
     */
    @param:Json(name = "SupportsExternalStream")
    val supportsExternalStream: Boolean,
    /**
     * The filename.
     */
    @param:Json(name = "Path")
    val path: String? = null,
    /**
     * The pixel format.
     */
    @param:Json(name = "PixelFormat")
    val pixelFormat: String? = null,
)
