package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlin.time.Clock

/**
 * 歌单音乐
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[PlaylistMusic]
 * @param [playlistId] 播放列表ID
 * @param [musicId] 音乐ID
 * @param [cachedAt] 缓存时间
 */
@Entity(
    primaryKeys = ["playlistId", "musicId", "connectionId","index"],
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
            childColumns = ["playlistId", "connectionId"],
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
        Index("playlistId"),
        Index(value = ["playlistId", "connectionId"]),
        Index(value = ["musicId", "connectionId"])
    ]
)
data class PlaylistMusic(
    val playlistId: String,
    val musicId: String,
    val connectionId: Long,
    val index:Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)
