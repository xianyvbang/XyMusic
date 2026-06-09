package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.Index
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
    indices = [Index("musicId"), Index("connectionId"), Index("artistId")]
)
data class ArtistMusic(
    val artistId: String,
    val musicId: String,
    val connectionId: Long,
    val index:Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)
