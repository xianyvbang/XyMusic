package cn.xybbz.localdata.data.album

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlin.time.Clock


/**
 * 艺术家专辑
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[ArtistAlbum]
 * @param [artistId] 艺术家ID
 * @param [cachedAt] 缓存时间
 */
@Entity(
    primaryKeys = ["artistId", "albumId", "connectionId"],
    foreignKeys = [
        ForeignKey(
            entity = ConnectionConfig::class,
            parentColumns = ["id"],
            childColumns = ["connectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = XyArtist::class,
            parentColumns = ["artistId", "connectionId"],
            childColumns = ["artistId", "connectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = XyAlbum::class,
            parentColumns = ["itemId", "connectionId"],
            childColumns = ["albumId", "connectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("albumId"),
        Index("connectionId"),
        Index("artistId"),
        Index(value = ["artistId", "connectionId"]),
        Index(value = ["albumId", "connectionId"])
    ]
)
data class ArtistAlbum(
    val artistId: String,
    val albumId: String,
    val connectionId: Long,
    val index: Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()// 缓存时间戳（System.currentTimeMillis）
)
