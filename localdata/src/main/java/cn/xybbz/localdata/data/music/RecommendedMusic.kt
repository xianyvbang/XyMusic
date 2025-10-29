package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import cn.xybbz.localdata.data.connection.ConnectionConfig


/**
 * 推荐音乐
 * @author xybbz
 * @date 2025/10/29
 * @constructor 创建[RecommendedMusic]
 * @param [musicId] 音乐 ID
 * @param [connectionId] 连接 ID
 * @param [index] 索引位置
 * @param [cachedAt] 缓存时间
 */
@Entity(
    primaryKeys = ["musicId", "connectionId", "index"],
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("musicId"), Index("connectionId")]
)
data class RecommendedMusic(
    val musicId: String,
    val connectionId: Long,
    val index: Int,
    val cachedAt: Long = System.currentTimeMillis()
)
