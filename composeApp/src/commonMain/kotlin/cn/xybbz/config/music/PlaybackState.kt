package cn.xybbz.config.music

import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum

/**
 * 播放器主状态。
 * 统一承载播放器页面需要观察的标量状态，避免同一语义分散在多个 Flow 中。
 */
data class PlaybackState(
    // 当前播放歌曲在原始列表中的索引
    val curOriginIndex: Int = Constants.MINUS_ONE_INT,
    // 当前播放歌曲在实际播放列表中的索引
    val curRealIndex: Int = Constants.MINUS_ONE_INT,
    // 当前已加载到的页码
    val pageNum: Int = 0,
    // 当前列表分页大小
    val pageSize: Int = 0,
    // 当前播放歌曲信息
    val musicInfo: XyPlayMusic? = null,
    // 当前歌曲封面字节流
    val picByte: ByteArray? = null,
    // 封面刷新版本号，用于触发 UI 重新读取封面
    val coverRefreshVersion: Int = 0,
    // 当前歌曲总时长
    val duration: Long = 0L,
    // 当前播放器状态
    val state: PlayStateEnum = PlayStateEnum.None,
    // 当前播放数据类型
    val playDataType: MusicPlayTypeEnum = MusicPlayTypeEnum.FOUNDATION,
    // 片头跳过时间
    val headTime: Long = 0L,
    // 片尾跳过时间
    val endTime: Long = 0L,
    // 当前播放模式
    val playMode: PlayerModeEnum = PlayerModeEnum.SEQUENTIAL_PLAYBACK
)
