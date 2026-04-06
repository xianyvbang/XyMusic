package cn.xybbz.localdata.data.album

import androidx.room.Entity
import androidx.room.Index
import cn.xybbz.localdata.data.genre.XyGenre
import kotlin.time.Clock

/**
 * 流派专辑
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[GenreAlbum]
 * @param [albumId] 专辑ID
 * @param [cachedAt] 缓存时间
 */
@Entity(
    primaryKeys = ["albumId", "genreId", "connectionId"],
    indices = [Index("albumId"), Index("connectionId"), Index("genreId")]
)
data class GenreAlbum(
    val genreId: String,
    val albumId: String,
    val connectionId: Long,
    val index:Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)
