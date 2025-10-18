package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.enums.jellyfin.BaseItemKind
import cn.xybbz.api.enums.jellyfin.MediaType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

/**
 * Class SearchHintResult.
 */
@JsonClass(generateAdapter = true)
public data class SearchHintResult(
    /**
     * The search hints.
     */
    @param:Json(name = "SearchHints")
    val searchHints: List<SearchHintResponse>,
    /**
     * The total record count.
     */
    @param:Json(name = "TotalRecordCount")
    val totalRecordCount: Int,
)


@JsonClass(generateAdapter = true)
data class SearchHintResponse(
    /**
     * The item id.
     */
    @Deprecated("This member is deprecated and may be removed in the future")
    @param:Json(name = "ItemId")
    val itemId: String,
    /**
     * The item id.
     */
    @param:Json(name = "Id")
    val id: String,
    /**
     * The name.
     */
    @param:Json(name = "Name")
    val name: String,
    /**
     * The matched term.
     */
    @param:Json(name = "MatchedTerm")
    val matchedTerm: String? = null,
    /**
     * The index number.
     */
    @param:Json(name = "IndexNumber")
    val indexNumber: Int? = null,
    /**
     * The production year.
     */
    @param:Json(name = "ProductionYear")
    val productionYear: Int? = null,
    /**
     * The parent index number.
     */
    @param:Json(name = "ParentIndexNumber")
    val parentIndexNumber: Int? = null,
    /**
     * The image tag.
     */
    @param:Json(name = "PrimaryImageTag")
    val primaryImageTag: String? = null,
    /**
     * The thumb image tag.
     */
    @param:Json(name = "ThumbImageTag")
    val thumbImageTag: String? = null,
    /**
     * The thumb image item identifier.
     */
    @param:Json(name = "ThumbImageItemId")
    val thumbImageItemId: String? = null,
    /**
     * The backdrop image tag.
     */
    @param:Json(name = "BackdropImageTag")
    val backdropImageTag: String? = null,
    /**
     * The backdrop image item identifier.
     */
    @param:Json(name = "BackdropImageItemId")
    val backdropImageItemId: String? = null,
    /**
     * The base item kind.
     */
    @param:Json(name = "Type")
    val type: BaseItemKind,
    /**
     * A value indicating whether this instance is folder.
     */
    @param:Json(name = "IsFolder")
    val isFolder: Boolean? = null,
    /**
     * The run time ticks.
     */
    @param:Json(name = "RunTimeTicks")
    val runTimeTicks: Long? = null,
    /**
     * Media types.
     */
    @param:Json(name = "MediaType")
    val mediaType: MediaType,
    /**
     * The start date.
     */
    @param:Json(name = "StartDate")
    val startDate: LocalDateTime? = null,
    /**
     * The end date.
     */
    @param:Json(name = "EndDate")
    val endDate: LocalDateTime? = null,
    /**
     * The series.
     */
    @param:Json(name = "Series")
    val series: String? = null,
    /**
     * The status.
     */
    @param:Json(name = "Status")
    val status: String? = null,
    /**
     * The album.
     */
    @param:Json(name = "Album")
    val album: String? = null,
    /**
     * The album id.
     */
    @param:Json(name = "AlbumId")
    val albumId: String? = null,
    /**
     * The album artist.
     */
    @param:Json(name = "AlbumArtist")
    val albumArtist: String? = null,
    /**
     * The artists.
     */
    @param:Json(name = "Artists")
    val artists: List<String>,
    /**
     * The song count.
     */
    @param:Json(name = "SongCount")
    val songCount: Int? = null,
    /**
     * The episode count.
     */
    @param:Json(name = "EpisodeCount")
    val episodeCount: Int? = null,
    /**
     * The channel identifier.
     */
    @param:Json(name = "ChannelId")
    val channelId: String? = null,
    /**
     * The name of the channel.
     */
    @param:Json(name = "ChannelName")
    val channelName: String? = null,
    /**
     * The primary image aspect ratio.
     */
    @param:Json(name = "PrimaryImageAspectRatio")
    val primaryImageAspectRatio: Double? = null,
)
