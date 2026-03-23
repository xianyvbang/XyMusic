package cn.xybbz.config.music

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.config.scope.IoScoped
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class MusicCommonController: IoScoped() {

    // 原始歌曲列表
    var originMusicList by mutableStateOf(emptyList<XyPlayMusic>())
        private set

    //当前播放歌曲的进度
    var musicCurrentPositionMap = mutableStateMapOf<String, Long>()
        private set

    // 当前播放的歌曲在原始歌曲列表中的索引
    var curOriginIndex by mutableIntStateOf(Constants.MINUS_ONE_INT)
        private set

    //加载的音乐最大页码
    var pageNum by mutableIntStateOf(0)
        private set

    var pageSize by mutableIntStateOf(0)
        private set

    //当前播放音乐信息
    var musicInfo by mutableStateOf<XyPlayMusic?>(null)
        private set

    var picByte: ByteArray? by mutableStateOf(null)
        private set

    var coverRefreshVersion by mutableIntStateOf(0)
        protected set

    //音频总时长
    var duration by mutableLongStateOf(0L)
        private set

    //当前状态
    var state by mutableStateOf(PlayStateEnum.None)
        private set

    //播放进度
    private val _progressStateFlow = MutableStateFlow(0L)
    val progressStateFlow = _progressStateFlow.asStateFlow()

    //当前播放数据类型
    var playDataType by mutableStateOf(MusicPlayTypeEnum.FOUNDATION)
        private set

    //片头跳过时间
    var headTime by mutableLongStateOf(0L)
        private set

    //片尾跳过时间
    var endTime by mutableLongStateOf(0L)
        private set

    //当前播放模式
    var playType by mutableStateOf(PlayerTypeEnum.SEQUENTIAL_PLAYBACK)
        private set

    var ifNextPage = true

    var ifGetNextPageMusicDataIsNullCount: Int = 0

    //事件发送流
    private val _events = MutableSharedFlow<PlayerEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val events = _events.asSharedFlow()

    /**
     * 列表中添加数据
     */
   abstract fun addMusicList(
        musicList: List<XyPlayMusic>,
        artistId: String? = null,
        isPlayer: Boolean? = null
    )


    /**
     * 更新当前音乐的收藏信息->更新UI数据
     */
  abstract  fun updateCurrentFavorite(isFavorite: Boolean)


    /**
     * 清空播放列表
     */
    abstract fun clearPlayerList()
}
