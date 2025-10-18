package cn.xybbz.api.client.subsonic.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlbumList2ID3(
    /**
     * 专辑信息
     */
    val album : List<AlbumID3>? = null,
)
