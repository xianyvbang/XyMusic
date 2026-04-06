package cn.xybbz.localdata.data.artist

import androidx.room.Entity
import androidx.room.Index
import kotlin.time.Clock

/**
 * 收藏音乐
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[FavoriteArtist]
 * @param [musicId] 音乐ID
 * @param [cachedAt] 缓存时间
 */
@Entity(
    primaryKeys = ["artistId", "connectionId"],
    indices = [Index("artistId"), Index("connectionId")]
)
data class FavoriteArtist(
    val artistId: String,
    val connectionId: Long,
    val ifFavorite: Boolean = true,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)