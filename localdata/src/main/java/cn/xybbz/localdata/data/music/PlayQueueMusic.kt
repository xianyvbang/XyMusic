package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 播放队列音乐
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[PlayQueueMusic]
 * @param [musicId] 音乐ID
 * @param [cachedAt] 缓存
 */
@Entity(
    primaryKeys = ["musicId", "connectionId"],
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("musicId"), Index("connectionId")]
)
data class PlayQueueMusic(
    val musicId: String,
    val picByte: ByteArray? = null,
    val connectionId: Long,
    val index: Int,
    val cachedAt: Long = System.currentTimeMillis()
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayQueueMusic) return false

        if (musicId != other.musicId) return false
        if (!(picByte?.contentEquals(other.picByte) ?: (other.picByte == null))) return false
        if (connectionId != other.connectionId) return false
        if (index != other.index) return false
        if (cachedAt != other.cachedAt) return false

        return true
    }


    override fun hashCode(): Int {
        var result = musicId.hashCode()
        result = 31 * result + (picByte?.contentHashCode() ?: 0)
        result = 31 * result + connectionId.hashCode()
        result = 31 * result + index
        result = 31 * result + cachedAt.hashCode()
        return result
    }

}
