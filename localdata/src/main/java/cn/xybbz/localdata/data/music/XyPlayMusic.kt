package cn.xybbz.localdata.data.music

import java.util.UUID

/**
 * 音乐嵌套类
 */
data class XyPlayMusic(
    val itemId: String,
    /**
     * 音乐图片
     */
    val pic: String?,
    /**
     * 音乐图片字节码
     */
    val picByte: ByteArray? = null,
    /**
     * 音乐名称
     */
    val name: String,

    /**
     * 专辑id
     */
    val album: String,
    /**
     * 音乐地址
     */
    val musicUrl: String,
    /**
     * 播放会话id,
     */
    val playSessionId: String = UUID.randomUUID().toString(),
    /**
     * 格式
     */
    val container: String?,
    /**
     * 音乐艺术家名称
     */
    val artists: String?,

    /**
     * 是否已经收藏
     */
    val ifFavoriteStatus: Boolean = false,
    /**
     * 大小
     */
    val size: Long?,
    /**
     * 文件地址
     */
    val filePath: String?,
    /**
     * 时长 秒
     */
    val runTimeTicks: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XyPlayMusic) return false

        return itemId == other.itemId &&
                pic == other.pic &&
                (picByte?.contentEquals(other.picByte) ?: (other.picByte == null)) &&
                name == other.name &&
                album == other.album &&
                musicUrl == other.musicUrl &&
                playSessionId == other.playSessionId &&
                container == other.container &&
                artists == other.artists &&
                ifFavoriteStatus == other.ifFavoriteStatus &&
                size == other.size &&
                filePath == other.filePath &&
                runTimeTicks == other.runTimeTicks
    }

    override fun hashCode(): Int {
        var result = itemId.hashCode()
        result = 31 * result + (pic?.hashCode() ?: 0)
        result = 31 * result + (picByte?.contentHashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + album.hashCode()
        result = 31 * result + musicUrl.hashCode()
        result = 31 * result + playSessionId.hashCode()
        result = 31 * result + (container?.hashCode() ?: 0)
        result = 31 * result + (artists?.hashCode() ?: 0)
        result = 31 * result + ifFavoriteStatus.hashCode()
        result = 31 * result + (size?.hashCode() ?: 0)
        result = 31 * result + (filePath?.hashCode() ?: 0)
        result = 31 * result + runTimeTicks.hashCode()
        return result
    }

}