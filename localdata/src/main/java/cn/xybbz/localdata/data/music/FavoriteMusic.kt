package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 收藏音乐
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[FavoriteMusic]
 * @param [musicId] 音乐ID
 * @param [cachedAt] 缓存时间
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
data class FavoriteMusic(
    val musicId: String,
    val connectionId: Long,
    val index:Int,
    val cachedAt: Long = System.currentTimeMillis()
)
