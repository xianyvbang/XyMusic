package cn.xybbz.localdata.data.album

import androidx.room.Entity
import androidx.room.Index
import kotlin.time.Clock

/**
 * 最多播放专辑
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[MaximumPlayAlbum]
 * @param [musicId] 音乐ID
 * @param [cachedAt] 缓存时间
 */
@Entity(
    primaryKeys = ["albumId", "connectionId"],
    indices = [Index("albumId"), Index("connectionId")],
)
data class MaximumPlayAlbum(
    val albumId: String,
    val connectionId: Long,
    val index:Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)
