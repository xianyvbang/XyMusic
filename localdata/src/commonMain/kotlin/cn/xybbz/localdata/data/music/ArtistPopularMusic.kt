package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlin.time.Clock

/**
 * 歌手热门歌曲缓存关系。
 */
@Entity(
    primaryKeys = ["artistKey", "musicId", "connectionId"],
    foreignKeys = [
        ForeignKey(
            entity = ConnectionConfig::class,
            parentColumns = ["id"],
            childColumns = ["connectionId"],
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
        Index("artistKey"),
        Index("musicId"),
        Index("connectionId"),
        Index(value = ["musicId", "connectionId"]),
        Index(value = ["connectionId", "artistKey", "cachedAt"])
    ]
)
data class ArtistPopularMusic(
    val artistKey: String,
    val musicId: String,
    val connectionId: Long,
    val index: Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)
