package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlin.time.Clock

/**
 * 艺术家音乐
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[ArtistMusic]
 * @param [artistId] 艺术家ID
 * @param [musicId] 音乐ID
 * @param [cachedAt] 缓存时间
 */
@Entity(
    primaryKeys = ["artistId", "musicId","connectionId"],
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
            entity = XyMusic::class,
            parentColumns = ["itemId", "connectionId"],
            childColumns = ["musicId", "connectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("musicId"),
        Index("connectionId"),
        Index("artistId"),
        Index(value = ["artistId", "connectionId"]),
        Index(value = ["musicId", "connectionId"])
    ]
)
data class ArtistMusic(
    val artistId: String,
    val musicId: String,
    val connectionId: Long,
    val index:Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)
