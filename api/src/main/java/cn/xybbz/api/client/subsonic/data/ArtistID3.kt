package cn.xybbz.api.client.subsonic.data

import com.squareup.moshi.JsonClass


/**
 * ArtistID3，An artist from ID3 tags.
 */
@JsonClass(generateAdapter = true)
data class ArtistID3 (
    /**
     * Artist album count.
     */
    val albumCount: Long? = null,

    /**
     * A covertArt id.
     */
    val coverArt: String? = null,

    /**
     * The id of the artist
     */
    val id: String,

    /**
     * The artist name.
     */
    val name: String,

    /**
     * 艺术家专辑
     */
    val album: List<AlbumID3>? = null
)
