package cn.xybbz.entity.data.music

import java.util.UUID

/**
 * 音乐嵌套类
 */
data class XyMusicExtend(
    val itemId: String = "",
    /**
     * 音乐图片
     */
    val pic: String? = "",
    /**
     * 音乐名称
     */
    val name: String = "",

    /**
     * 专辑id
     */
    val album: String = "",
    /**
     * 音乐地址
     */
    val musicUrl: String = "",
    /**
     * 播放会话id,
     */
    val playSessionId: String = UUID.randomUUID().toString(),
    /**
     * 格式
     */
    val container: String? = "",
    /**
     * 音乐艺术家名称
     */
    val artists: String? = "",

    /**
     * 是否已经收藏
     */
    val ifFavoriteStatus: Boolean,
    /**
     * 大小
     */
    val size: Long? = 0,
    /**
     * 文件地址
     */
    val filePath:String? = null
)
