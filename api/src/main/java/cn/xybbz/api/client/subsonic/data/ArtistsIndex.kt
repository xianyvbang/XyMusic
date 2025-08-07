package cn.xybbz.api.client.subsonic.data

import com.squareup.moshi.JsonClass

/**
 * 艺术家索引对象
 */
@JsonClass(generateAdapter = true)
data class ArtistsIndex(

    /**
     * 索引名称
     */
    val name:String,

    /**
     * 索引中的艺术家
     */
    val artist: List<ArtistID3>

)
