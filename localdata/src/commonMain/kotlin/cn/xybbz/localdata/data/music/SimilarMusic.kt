package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlin.time.Clock

/**
 * 相似歌曲缓存关系。
 */
@Entity(
    primaryKeys = ["sourceMusicId", "musicId", "connectionId"],
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
            childColumns = ["sourceMusicId", "connectionId"],
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
        Index("sourceMusicId"),
        Index("musicId"),
        Index("connectionId"),
        Index(value = ["sourceMusicId", "connectionId"]),
        Index(value = ["musicId", "connectionId"]),
        Index(value = ["connectionId", "sourceMusicId", "cachedAt"])
    ]
)
data class SimilarMusic(
    val sourceMusicId: String,
    val musicId: String,
    val connectionId: Long,
    val index: Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)
