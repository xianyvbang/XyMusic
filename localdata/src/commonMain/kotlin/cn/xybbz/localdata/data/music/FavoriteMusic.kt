package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.Index
import kotlin.time.Clock

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
    indices = [Index("musicId"), Index("connectionId")]
)
data class FavoriteMusic(
    val musicId: String,
    val connectionId: Long,
    val ifFavorite: Boolean = true,
    val index:Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)
