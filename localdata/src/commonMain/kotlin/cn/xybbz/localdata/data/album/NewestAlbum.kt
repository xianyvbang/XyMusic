package cn.xybbz.localdata.data.album

import androidx.room.Entity
import androidx.room.Index
import kotlin.time.Clock

/**
 * 最新专辑
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[NewestAlbum]
 * @param [albumId] 专辑ID
 * @param [cachedAt] 缓存时间
 */
@Entity(
    primaryKeys = ["albumId", "connectionId"],
    indices = [Index("albumId"), Index("connectionId")]
)
data class NewestAlbum(
    val albumId: String,
    val connectionId: Long,
    val index:Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)
