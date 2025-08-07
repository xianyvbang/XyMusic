package cn.xybbz.api.client.subsonic.data

import com.squareup.moshi.JsonClass

/**
 * ArtistsID3，A list of indexed Artists.
 */
@JsonClass(generateAdapter = true)
data class ArtistsID3 (
    /**
     * List of ignored articles space separated
     */
    val ignoredArticles: String? = null,

    /**
     * Index list
     */
    val index: List<ArtistsIndex>? = null
)