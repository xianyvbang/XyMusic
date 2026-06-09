package cn.xybbz.api.client.subsonic.data

import kotlinx.serialization.Serializable

@Serializable
data class AlbumInfo2ID3(
    val notes: String? = null,
    val musicBrainzId: String? = null,
    val lastFmUrl: String? = null,
    val smallImageUrl: String? = null,
    val mediumImageUrl: String? = null,
    val largeImageUrl: String? = null,
)
