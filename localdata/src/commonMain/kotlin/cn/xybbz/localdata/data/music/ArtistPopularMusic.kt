package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.Index
import kotlin.time.Clock

/**
 * 歌手热门歌曲缓存关系。
 */
@Entity(
    primaryKeys = ["artistKey", "musicId", "connectionId"],
    indices = [
        Index("artistKey"),
        Index("musicId"),
        Index("connectionId"),
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
