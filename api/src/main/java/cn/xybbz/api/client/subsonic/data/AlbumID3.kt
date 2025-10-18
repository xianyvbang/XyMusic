package cn.xybbz.api.client.subsonic.data

import java.time.LocalDateTime

/**
 * AlbumID3，Album with songs.
 */
data class AlbumID3  (
    /**
     * 专辑编码
     */
    val id: String,
    /**
     * 专辑艺术家编码
     */
    val artistId: String,
    /**
     * 专辑名称
     */
    val name: String,
    /**
     * 艺术家名称
     */
    val artist: String,
    /**
     * 创建时间
     */
    val created: LocalDateTime,
    /**
     * 持续时间
     */
    val duration: Long,
    /**
     * 歌曲数量
     */
    val songCount: Long,
    /**
     * 封面图片编码
     */
    val coverArt: String? = null,
    /**
     * 专辑歌曲
     */
    val song: List<SongID3>? = null
)