package cn.xybbz.api.client.subsonic.data

data class GenreID3(
    /**
     * Genre album count
     */
    val albumCount: Long,

    /**
     * Genre song count
     */
    val songCount: Long,

    /**
     * Genre name
     */
    val value: String
)
