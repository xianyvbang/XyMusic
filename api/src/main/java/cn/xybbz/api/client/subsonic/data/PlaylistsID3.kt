package cn.xybbz.api.client.subsonic.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlaylistsID3(
    /**
     * 专辑歌曲
     */
    val playlist: List<PlaylistID3>? = null
)
