package cn.xybbz.api.client.jellyfin.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LyricResponse(
    /**
     * Metadata for the lyrics.
     */
    @param:Json(name = "Metadata")
    val metadata: LyricMetadata,
    /**
     * A collection of individual lyric lines.
     */
    @param:Json(name = "Lyrics")
    val lyrics: List<LyricLine>,
)

@JsonClass(generateAdapter = true)
data class LyricMetadata(
    /**
     * The lyric offset compared to audio in ticks.
     */
    @param:Json(name = "Offset")
    val offset: Long? = null,
)

/**
 * Lyric model.
 */
@JsonClass(generateAdapter = true)
data class LyricLine(
    /**
     * The text of this lyric line.
     */
    @param:Json(name = "Text")
    val text: String,
    /**
     * The start time in ticks.
     */
    @param:Json(name = "Start")
    val start: Long? = null,
)