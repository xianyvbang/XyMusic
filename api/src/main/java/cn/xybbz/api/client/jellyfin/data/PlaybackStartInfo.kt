package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.enums.jellyfin.PlayMethod
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * Class PlaybackStartInfo.
 */
@JsonClass(generateAdapter = true)
data class PlaybackStartInfo(
    /**
     * A value indicating whether this instance can seek.
     */
    @param:Json(name = "CanSeek")
    val canSeek: Boolean,
    /**
     * The item.
     */
    @param:Json(name = "Item")
    val item: ItemResponse? = null,
    /**
     * The item identifier.
     */
    @param:Json(name = "ItemId")
    val itemId: String,
    /**
     * The session id.
     */
    @param:Json(name = "SessionId")
    val sessionId: String? = null,
    /**
     * The media version identifier.
     */
    @param:Json(name = "MediaSourceId")
    val mediaSourceId: String? = null,
    /**
     * The index of the audio stream.
     */
    @param:Json(name = "AudioStreamIndex")
    val audioStreamIndex: Int? = null,
    /**
     * The index of the subtitle stream.
     */
    @param:Json(name = "SubtitleStreamIndex")
    val subtitleStreamIndex: Int? = null,
    /**
     * A value indicating whether this instance is paused.
     */
    @param:Json(name = "IsPaused")
    val isPaused: Boolean,
    /**
     * A value indicating whether this instance is muted.
     */
    @param:Json(name = "IsMuted")
    val isMuted: Boolean,
    /**
     * The position ticks.
     */
    @param:Json(name = "PositionTicks")
    val positionTicks: Long? = null,
    @param:Json(name = "PlaybackStartTimeTicks")
    val playbackStartTimeTicks: Long? = null,
    /**
     * The volume level.
     */
    @param:Json(name = "VolumeLevel")
    val volumeLevel: Int? = null,
    @param:Json(name = "Brightness")
    val brightness: Int? = null,
    @param:Json(name = "AspectRatio")
    val aspectRatio: String? = null,
    /**
     * The play method.
     */
    @param:Json(name = "PlayMethod")
    val playMethod: PlayMethod,
    /**
     * The live stream identifier.
     */
    @param:Json(name = "LiveStreamId")
    val liveStreamId: String? = null,
    /**
     * The play session identifier.
     */
    @param:Json(name = "PlaySessionId")
    val playSessionId: String? = null,
    @param:Json(name = "PlaylistItemId")
    val playlistItemId: String? = null,
)
