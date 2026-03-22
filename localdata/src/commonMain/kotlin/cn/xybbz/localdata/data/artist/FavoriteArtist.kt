package cn.xybbz.localdata.data.artist

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import cn.xybbz.localdata.data.connection.ConnectionConfig

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
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = ForeignKey.Companion.CASCADE
    ),
        ForeignKey(
            entity = XyArtist::class,
            parentColumns = ["artistId"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.Companion.CASCADE
        )],
    indices = [Index("artistId"), Index("connectionId")]
)
data class FavoriteArtist(
    val artistId: String,
    val connectionId: Long,
    val ifFavorite: Boolean = true,
    val cachedAt: Long = System.currentTimeMillis()
)