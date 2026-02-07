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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LyricResponse(
    /**
     * Metadata for the lyrics.
     */
    @SerialName(value = "Metadata")
    val metadata: LyricMetadata,
    /**
     * A collection of individual lyric lines.
     */
    @SerialName(value = "Lyrics")
    val lyrics: List<LyricLine>,
)

@Serializable
data class LyricMetadata(
    /**
     * The lyric offset compared to audio in ticks.
     */
    @SerialName(value = "Offset")
    val offset: Long? = null,
)

/**
 * Lyric model.
 */
@Serializable
data class LyricLine(
    /**
     * The text of this lyric line.
     */
    @SerialName(value = "Text")
    val text: String,
    /**
     * The start time in ticks.
     */
    @SerialName(value = "Start")
    val start: Long? = null,
)