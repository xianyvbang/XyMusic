package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.Index
import kotlin.time.Clock

/**
 * 专辑音乐
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[AlbumMusic]
 * @param [albumId] 专辑ID
 * @param [musicId] 音乐ID
 * @param [cachedAt] 缓存时间
 */
@Entity(
    primaryKeys = ["albumId", "musicId", "connectionId"],
    indices = [Index("albumId"), Index("connectionId"), Index("musicId")]
)
data class AlbumMusic(
    val albumId: String,
    val musicId: String,
    val connectionId: Long,
    val index: Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()// 缓存时间戳（System.currentTimeMillis）
)
