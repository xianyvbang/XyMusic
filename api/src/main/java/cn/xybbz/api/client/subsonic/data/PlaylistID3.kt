package cn.xybbz.api.client.subsonic.data

import java.time.LocalDateTime

data class PlaylistID3(
    /**
     * 歌单编码
     */
    val id: String,
    /**
     * 歌单名称
     */
    val name: String,
    /**
     * 歌单评价
     */
    val comment: String,
    /**
     * 所属用户
     */
    val owner: String,
    /**
     * 是否公开
     */
    val public: Boolean,
    /**
     * 歌曲数量
     */
    val songCount: Long,
    /**
     * 创建时间
     */
    val created: LocalDateTime,
    /**
     * 封面图片编码
     */
    val coverArt: String,
    /**
     * 专辑歌曲
     */
    val entry: List<SongID3>? = null
)
