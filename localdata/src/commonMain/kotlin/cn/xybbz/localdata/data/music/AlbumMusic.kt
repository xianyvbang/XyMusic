package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.connection.ConnectionConfig
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
    foreignKeys = [
        ForeignKey(
            entity = ConnectionConfig::class,
            parentColumns = ["id"],
            childColumns = ["connectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = XyAlbum::class,
            parentColumns = ["itemId", "connectionId"],
            childColumns = ["albumId", "connectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = XyMusic::class,
            parentColumns = ["itemId", "connectionId"],
            childColumns = ["musicId", "connectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("albumId"),
        Index("connectionId"),
        Index("musicId"),
        Index(value = ["albumId", "connectionId"]),
        Index(value = ["musicId", "connectionId"]),
        Index(value = ["connectionId", "index", "musicId"]),
        Index(value = ["albumId", "connectionId", "index"])
    ]
)
data class AlbumMusic(
    val albumId: String,
    val musicId: String,
    val connectionId: Long,
    val index: Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()// 缓存时间戳（System.currentTimeMillis）
)
