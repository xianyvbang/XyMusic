package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlin.time.Clock

/**
 * 播放历史音乐
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[PlayHistoryMusic]
 * @param [musicId] 音乐ID
 * @param [cachedAt] 缓存时间
 */
@Entity(
    primaryKeys = ["musicId", "connectionId"],
    foreignKeys = [
        ForeignKey(
            entity = ConnectionConfig::class,
            parentColumns = ["id"],
            childColumns = ["connectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = XyMusic::class,
            parentColumns = ["itemId", "connectionId"],
            childColumns = ["musicId", "connectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("musicId"), Index("connectionId"), Index(value = ["musicId", "connectionId"])]
)
data class PlayHistoryMusic(
    val musicId: String,
    val connectionId: Long,
    val index:Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)
