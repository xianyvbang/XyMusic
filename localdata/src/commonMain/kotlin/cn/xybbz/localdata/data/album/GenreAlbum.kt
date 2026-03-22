package cn.xybbz.localdata.data.album

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.genre.XyGenre

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
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    ), ForeignKey(
        entity = XyGenre::class,
        parentColumns = ["itemId"],
        childColumns = ["genreId"],
        onDelete = CASCADE
    )],
    indices = [Index("albumId"), Index("connectionId"), Index("genreId")]
)
data class GenreAlbum(
    val genreId: String,
    val albumId: String,
    val connectionId: Long,
    val index:Int,
    val cachedAt: Long = System.currentTimeMillis()
)
