package cn.xybbz.api.client.subsonic.data

data class SongID3(
    /**
     * 歌曲编码
     */
    val id: String,
    /**
     * 是否是文件夹
     */
    val isDir: Boolean,
    /**
     * 歌曲名称
     */
    val title: String,
    /**
     * 专辑名称
     */
    val album: String,
    /**
     * 艺术家名称
     */
    val artist: String,
    /**
     * 音轨
     */
    val track: Long? = null,
    /**
     * 图片类型
     */
    val contentType: String,
    /**
     * 文件后缀
     */
    val suffix: String,
    /**
     * 文件路径
     */
    val path: String,
    /**
     * 持续时间/秒
     */
    val duration: Long,
    /**
     * 创建书剑
     */
    val created: String,
    /**
     * 专辑编码
     */
    val albumId: String,
    /**
     * 艺术家编码
     */
    val artistId: String,
    /**
     * 数据类型
     */
    val type: String,
    /**
     * 图片编码
     */
    val coverArt: String? = null,
    /**
     * 比特率
     */
    val bitRate: Int,
    /**
     * 大小/字节(B)
     */
    val size: Long,
    /**
     * 所属年代
     */
    val year: Int?,
    /**
     * 流派
     */
    val genre: String? = null,
    /**
     * 收藏信息
     */
    val starred: String? = null
)
