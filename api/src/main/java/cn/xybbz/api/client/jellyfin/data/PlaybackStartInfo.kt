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

import cn.xybbz.api.enums.jellyfin.PlayMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Class PlaybackStartInfo.
 */
@Serializable
data class PlaybackStartInfo(
    /**
     * A value indicating whether this instance can seek.
     */
    @SerialName(value = "CanSeek")
    val canSeek: Boolean,
    /**
     * The item.
     */
    @SerialName(value = "Item")
    val item: ItemResponse? = null,
    /**
     * The item identifier.
     */
    @SerialName(value = "ItemId")
    val itemId: String,
    /**
     * The session id.
     */
    @SerialName(value = "SessionId")
    val sessionId: String? = null,
    /**
     * The media version identifier.
     */
    @SerialName(value = "MediaSourceId")
    val mediaSourceId: String? = null,
    /**
     * The index of the audio stream.
     */
    @SerialName(value = "AudioStreamIndex")
    val audioStreamIndex: Int? = null,
    /**
     * The index of the subtitle stream.
     */
    @SerialName(value = "SubtitleStreamIndex")
    val subtitleStreamIndex: Int? = null,
    /**
     * A value indicating whether this instance is paused.
     */
    @SerialName(value = "IsPaused")
    val isPaused: Boolean,
    /**
     * A value indicating whether this instance is muted.
     */
    @SerialName(value = "IsMuted")
    val isMuted: Boolean,
    /**
     * The position ticks.
     */
    @SerialName(value = "PositionTicks")
    val positionTicks: Long? = null,
    @SerialName(value = "PlaybackStartTimeTicks")
    val playbackStartTimeTicks: Long? = null,
    /**
     * The volume level.
     */
    @SerialName(value = "VolumeLevel")
    val volumeLevel: Int? = null,
    @SerialName(value = "Brightness")
    val brightness: Int? = null,
    @SerialName(value = "AspectRatio")
    val aspectRatio: String? = null,
    /**
     * The play method.
     */
    @SerialName(value = "PlayMethod")
    val playMethod: PlayMethod,
    /**
     * The live stream identifier.
     */
    @SerialName(value = "LiveStreamId")
    val liveStreamId: String? = null,
    /**
     * The play session identifier.
     */
    @SerialName(value = "PlaySessionId")
    val playSessionId: String? = null,
    @SerialName(value = "PlaylistItemId")
    val playlistItemId: String? = null,
)
