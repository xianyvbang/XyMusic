package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.Index
import cn.xybbz.localdata.data.album.XyAlbum
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
    indices = [Index("musicId"), Index("connectionId"), Index("playlistId")]
)
data class PlaylistMusic(
    val playlistId: String,
    val musicId: String,
    val connectionId: Long,
    val index:Int,
    val cachedAt: Long = Clock.System.now().toEpochMilliseconds()
)
