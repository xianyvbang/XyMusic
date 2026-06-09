package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.Index
import kotlin.time.Clock

/**
 * 相似歌曲缓存关系。
 */
@Entity(
    primaryKeys = ["sourceMusicId", "musicId", "connectionId"],
    indices = [
        Index("sourceMusicId"),
        Index("musicId"),
        Index("connectionId"),
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
