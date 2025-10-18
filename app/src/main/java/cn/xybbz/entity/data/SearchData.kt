package cn.xybbz.entity.data

import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic

/**
 * 搜索内容类
 * @author xybbz
 * @date 2025/06/15
 * @constructor 创建[SearchData]
 * @param [musics]
 * @param [albums]
 * @param [artists]
 */
data class SearchData(
    /**
     * 音乐列表
     */
    var musics: List<XyMusic>? = null,
    /**
     * 专辑列表
     */
    var albums: List<XyAlbum>? = null,
    /**
     * 艺术家列表
     */
    var artists: List<XyArtist>? = null
)
