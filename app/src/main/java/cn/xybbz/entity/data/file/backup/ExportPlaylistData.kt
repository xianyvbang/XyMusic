package cn.xybbz.entity.data.file.backup

import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.type.PlaylistMusic
import com.squareup.moshi.JsonClass

/**
 * 导出歌单数据对象
 */
@JsonClass(generateAdapter = true)
data class ExportPlaylistData(
    /**
     * 歌单列表
     */
    val playlist: List<XyAlbum>,
    /**
     * 歌单和音乐关联表
     */
    val playlistMusic: List<PlaylistMusic>,
    /**
     * 歌单音乐
     */
    val musicList: List<XyMusic>,
)