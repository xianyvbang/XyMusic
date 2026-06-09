package cn.xybbz.localdata.data.album

import androidx.room.Entity
import androidx.room.Index
import kotlin.time.Clock

/**
 * 收藏音乐
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[FavoriteAlbum]
 * @param [cachedAt] 缓存时间
 */
@Entity(
    primaryKeys = ["albumId", "connectionId"],
    indices = [Index("albumId"), Index("connectionId")]
)
data class FavoriteAlbum(
    val albumId: String,
    val connectionId: Long,
    val ifFavorite: Boolean = true,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)
