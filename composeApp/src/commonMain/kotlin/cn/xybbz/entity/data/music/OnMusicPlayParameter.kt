package cn.xybbz.entity.data.music

import androidx.compose.runtime.Stable

/**
 * 播放音乐方法参数类
 * @author xybbz
 * @date 2024/06/15
 * @constructor 创建[OnMusicPlayParameter]
 * @param [index] 播放索引位置
 * @param [musicId] 音乐id
 * @param [albumId] 专辑id
 */
@Stable
data class OnMusicPlayParameter(
    var musicId: String,
    val albumId: String? = null,
    val artistId: String? = null
)
