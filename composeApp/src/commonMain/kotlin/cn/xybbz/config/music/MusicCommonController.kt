package cn.xybbz.config.music

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.enums.AudioCodecEnum
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.config.scope.IoScoped
import cn.xybbz.config.setting.OnSettingsChangeListener
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.entity.data.music.TranscodingAndMusicUrlData
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

abstract class MusicCommonController : IoScoped(), KoinComponent {

    val settingsManager = get<SettingsManager>()

    val downloadCacheController: DownloadCacheCommonController = get()

    val dataSourceManager: DataSourceManager = get()

    // 原始歌曲列表
    private val _originMusicList = mutableStateListOf<XyPlayMusic>()

    val originMusicList: List<XyPlayMusic> get() = _originMusicList

    //播放音乐列表 todo 应该存放拼接后的连接
    private val _playMusicList = mutableStateListOf<XyPlayMusic>()
    val playMusicList: List<XyPlayMusic> get() = _playMusicList

    //当前播放歌曲的进度
    var musicCurrentPositionMap = mutableStateMapOf<String, Long>()
        private set

    // 当前播放的歌曲在原始歌曲列表中的索引
    var curOriginIndex by mutableIntStateOf(Constants.MINUS_ONE_INT)
        private set

    // 当前播放的歌曲在当前播放模式下的实际歌曲列表中的索引
    var curRealIndex by mutableStateOf(Constants.MINUS_ONE_INT)
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

    //封面图刷新版本号
    var coverRefreshVersion by mutableIntStateOf(0)
        private set

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
        protected set

    //片尾跳过时间
    var endTime by mutableLongStateOf(0L)
        protected set

    //当前播放模式
    var playMode by mutableStateOf(PlayerModeEnum.SEQUENTIAL_PLAYBACK)
        protected set

    //是否能加载下一页
    var ifNextPage = true
        private set

    //加载下一页数据为空次数
    var ifGetNextPageMusicDataIsNullCount: Int = 0

    /**
     * 是否能操作playMusicList
     */
    open val isPlayMusicListMutable: Boolean = false

    //事件发送流
    private val _events = MutableSharedFlow<PlayerEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val events = _events.asSharedFlow()


    init {
        createScope()
        settingsManager.setOnListener(object : OnSettingsChangeListener {
            override fun onCacheMaxBytesChanged(
                cacheUpperLimit: CacheUpperLimitEnum,
                oldCacheUpperLimit: CacheUpperLimitEnum
            ) {
                if (oldCacheUpperLimit == CacheUpperLimitEnum.No && cacheUpperLimit != CacheUpperLimitEnum.No && state == PlayStateEnum.Playing) {
                    musicInfo?.let {
                        startCache(it, settingsManager.getStatic())
                    }
                }
            }

            override fun onMusicResourceConfigChanged() {
                scope.launch {
                    refreshPlaylistCoverMetadata()
                }
            }
        })
    }


    /**
     * 初始化播放
     */
    abstract fun initController(onRestorePlaylists: (() -> Unit)? = null)

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
    open fun updateCurrentFavorite(isFavorite: Boolean) {
        updateCurrentMusic(musicInfo?.copy(ifFavoriteStatus = isFavorite))
    }


    /**
     * 暂停当前播放
     */
    abstract fun pause()

    /**
     * 恢复当前播放
     */
    abstract fun resume()

    /**
     * 跳转播放到指定位置
     */
    abstract fun seekTo(millSeconds: Long)

    /**
     * 获取当前播放模式下的下一首歌曲
     */
    abstract fun seekToNext()

    /**
     * 获取当前播放模式下的上一首歌曲
     */
    abstract fun seekToPrevious()

    /**
     * 跳转至指定index位置音乐
     */
    abstract fun seekToIndex(index: Int)

    /**
     * 根据音乐id跳转
     */
    abstract fun seekToItemId(itemId: String)

    /**
     * 删除指定index位置音乐
     */
    open fun removeItem(index: Int) {
        removeMusic(index)
    }

    /**
     * 设置倍速
     */
    abstract fun setDoubleSpeed(value: Float)

    /**
     * 设置播放类型
     */
    open fun setPlayTypeData(playerModeEnum: PlayerModeEnum) {
        playMode = playerModeEnum
        updateEvent(PlayerEvent.PlayerTypeChange(playerModeEnum))
        updatePlayerMode()
        if (isPlayMusicListMutable)
            insertPlayMusicList(_originMusicList)
    }


    /**
     * 添加下一首播放功能
     */
    abstract fun addNextPlayer(music: XyPlayMusic)

    /**
     * 获取下一首播放位置的索引
     */
    open fun getNextPlayableIndex(): Int {
        if (isPlayMusicListMutable) {
            if (_playMusicList.isEmpty() || curRealIndex !in _playMusicList.indices) {
                return Constants.MINUS_ONE_INT
            }
            val nextIndex = if (curRealIndex == _playMusicList.lastIndex) 0 else curRealIndex + 1
            return nextIndex.takeIf { it in _playMusicList.indices } ?: Constants.MINUS_ONE_INT
        } else {
            if (_originMusicList.isEmpty() || curOriginIndex !in _originMusicList.indices) {
                return Constants.MINUS_ONE_INT
            }
            val nextIndex =
                if (curOriginIndex == _originMusicList.lastIndex) 0 else curOriginIndex + 1
            return nextIndex.takeIf { it in _originMusicList.indices } ?: Constants.MINUS_ONE_INT
        }

    }

    /**
     * 获取上一首播放位置的索引
     */
    open fun getPreviousPlayableIndex(): Int? {
        if (isPlayMusicListMutable) {
            if (_playMusicList.isEmpty() || curRealIndex !in _playMusicList.indices) {
                return null
            }
            val previousIndex =
                if (curRealIndex == 0) _playMusicList.lastIndex else curRealIndex - 1
            return previousIndex.takeIf { it in _playMusicList.indices }
        } else {
            if (_originMusicList.isEmpty() || curOriginIndex !in _originMusicList.indices) {
                return null
            }
            val previousIndex =
                if (curOriginIndex == 0) _originMusicList.lastIndex else curOriginIndex - 1
            return previousIndex.takeIf { it in _originMusicList.indices }
        }
    }

    /**
     * 替换音乐播放连接
     */
    abstract fun replacePlaylistItemUrl()

    /**
     * 添加音乐到列表
     */
    fun addMusic(music: XyPlayMusic, isPlayer: Boolean? = null) {
        addNextPlayer(music)
        if (isPlayer == true) {
            seekToNext()
        }
    }


    /**
     * 设置跳过片头片尾时间
     */
    open fun setHeadAndEntTime(headTime: Long, endTime: Long) {
        this.headTime = headTime
        this.endTime = endTime
    }


    /**
     * 设置当前音乐列表
     */
    open fun initMusicList(
        musicDataList: List<XyPlayMusic>,
        musicCurrentPositionMapData: Map<String, Long>?,
        originIndex: Int?,
        pageNum: Int,
        pageSize: Int,
        artistId: String?,
        ifInitPlayerList: Boolean = false,
        musicPlayTypeEnum: MusicPlayTypeEnum
    ) {
        updatePlayDataType(musicPlayTypeEnum)
        updateRestartCount()
        if (musicCurrentPositionMapData != null) {
            musicCurrentPositionMap.clear()
            musicCurrentPositionMap.putAll(musicCurrentPositionMapData)
        }
        replacePlaylist(musicDataList)
        setPageNumData(pageNum)
        updatePageSize(pageSize)
    }

    protected abstract fun refreshPlaylistCoverMetadata()

    /**
     * 各自播放器播放模式设置
     */
    protected abstract fun updatePlayerMode()


    /**
     * 开始缓存
     */
    fun startCache(music: XyPlayMusic, ifStatic: Boolean) {
        if (music.filePath.isNullOrBlank())
            downloadCacheController.cacheMedia(music, ifStatic)
    }

    /**
     * 设置当前播放进度
     */
    fun setCurrentPositionData(currentPosition: Long) {
        _progressStateFlow.value = currentPosition
    }

    /**
     * 调用onFavorite
     */
    fun invokingOnFavorite(itemId: String) {
        updateEvent(PlayerEvent.Favorite(itemId))
    }

    fun updateState(state: PlayStateEnum) {
        this.state = state
    }

    fun updateDuration(duration: Long) {
        this.duration = duration
    }


    /**
     * 更新获取下一页音乐数据为空的次数
     */
    fun updateIfGetNextPageMusicDataIsNullCount(count: Int) {
        this.ifGetNextPageMusicDataIsNullCount += count
        if (this.ifGetNextPageMusicDataIsNullCount >= 3) {
            updateIfNextPage(false)
        }
    }

    fun updateRestartCount() {
        this.ifGetNextPageMusicDataIsNullCount = 0
        updateIfNextPage(true)
    }

    /**
     * 更新是否可以加载下一页音乐数据
     */
    private fun updateIfNextPage(ifNextPage: Boolean) {
        this.ifNextPage = ifNextPage
    }


    fun updateEvent(event: PlayerEvent) {
        scope.launch {
            _events.emit(event)
        }
    }


    /**
     * 设置PageNum
     */
    fun setPageNumData(pageNum: Int) {
        this.pageNum = pageNum
    }


    fun reportedPlayEvent() {
        musicInfo?.let {
            updateEvent(PlayerEvent.Play(it.itemId, settingsManager.get().playSessionId))
        }
    }

    fun reportedPauseEvent() {
        musicInfo?.let {
            scope.launch {
                _events.emit(PlayerEvent.Pause(it.itemId, settingsManager.get().playSessionId))
            }
        }
    }

    /**
     * 更新列表数据
     */
    fun replacePlaylist(musicList: List<XyPlayMusic>) {
        _originMusicList.clear()
        _originMusicList.addAll(musicList)
        if (isPlayMusicListMutable)
            insertPlayMusicList(musicList)
    }

    fun addMusic(music: XyPlayMusic) {
        _originMusicList.add(music)
        if (isPlayMusicListMutable)
            _playMusicList.add(music)
    }

    fun insertMusic(music: XyPlayMusic) {
        val insertIndex = curOriginIndex + 1
        _originMusicList.add(insertIndex, music)
        if (isPlayMusicListMutable) {
            val playIndex = curRealIndex + 1
            _playMusicList.add(playIndex, music)
        }

    }

    fun addMusicList(musicList: List<XyPlayMusic>) {
        val insertIndex = curOriginIndex + 1
        _originMusicList.addAll(insertIndex, musicList)
        if (isPlayMusicListMutable) {
            val playIndex = curRealIndex + 1
            _playMusicList.addAll(playIndex, musicList)
        }
    }

    /**
     * 删除指定索引位置的数据
     */
    fun removeMusic(index: Int) {
        if (index !in _originMusicList.indices) {
            return
        }
        val playMusic = _originMusicList.removeAt(index)
        if (isPlayMusicListMutable)
            _playMusicList.remove(playMusic)
    }

    /**
     * 更新播放音乐的原始索引
     */
    fun updateOriginIndex(originIndex: Int) {
        if (originIndex != Constants.MINUS_ONE_INT) {
            curOriginIndex = originIndex
            if (isPlayMusicListMutable)
                curRealIndex =
                    _playMusicList.indexOfFirst { it.itemId == originMusicList[originIndex].itemId }
            syncCurrentMusicAfterOriginIndexChanged()
        }

    }

    /**
     * 更新播放索引
     */
    fun updateRealIndex(realIndex: Int) {
        if (realIndex != Constants.MINUS_ONE_INT) {
            curRealIndex = realIndex
            if (isPlayMusicListMutable)
                curOriginIndex =
                    originMusicList.indexOfFirst { it.itemId == _playMusicList[realIndex].itemId }
            syncCurrentMusicAfterOriginIndexChanged()
        }
    }


    /**
     * 更新播放列表数据
     */
    private fun insertPlayMusicList(musicList: List<XyPlayMusic>) {
        _playMusicList.clear()
        when (playMode) {
            PlayerModeEnum.SINGLE_LOOP -> {
                _playMusicList.addAll(musicList)
            }

            PlayerModeEnum.SEQUENTIAL_PLAYBACK -> {
                _playMusicList.addAll(musicList)
            }

            PlayerModeEnum.RANDOM_PLAY -> {
                _playMusicList.addAll(musicList.shuffled())
            }
        }
    }


    /**
     * 上报加载下一页事件,更新当前播放音乐信息,更新当前音乐的时长
     */
    private fun syncCurrentMusicAfterOriginIndexChanged() {
        if (originMusicList.isNotEmpty() && curOriginIndex >= originMusicList.size - 1 && ifNextPage) {
            updateEvent(PlayerEvent.NextList(pageNum))
        }
        val music = originMusicList[curOriginIndex]
        updateCurrentMusic(music)
        updateDuration(musicInfo?.runTimeTicks ?: 0L)
        updateEvent(
            PlayerEvent.ChangeMusic(
                music.itemId,
                music.artistIds?.firstOrNull(),
                music.artists?.firstOrNull()
            )
        )
    }

    /**
     * 更新coverRefreshVersion版本号
     */
    fun updateCoverRefreshVersion(version: Int) {
        coverRefreshVersion += version
    }

    /**
     * 更新当前播放音乐
     */
    private fun updateCurrentMusic(music: XyPlayMusic?) {
        this.musicInfo = music
    }

    /**
     * 更新当前页面字节码
     */
    fun updatePicBytes(picBytes: ByteArray?) {
        this.picByte = picBytes
    }

    /**
     * 更新分页大小
     */
    fun updatePageSize(pageSize: Int) {
        this.pageSize = pageSize
    }

    /**
     * 更新当前播放数据类型
     */
    open fun updatePlayDataType(playDataType: MusicPlayTypeEnum) {
        this.playDataType = playDataType
    }

    protected open fun getMusicUrl(
        musicId: String,
        plexPlayKey: String?
    ): TranscodingAndMusicUrlData {
        val audioBitRate = settingsManager.getAudioBitRate()

        val static: Boolean =
            settingsManager.getStatic()

        val musicUrl = dataSourceManager.getMusicPlayUrl(
            if (static) musicId else plexPlayKey ?: musicId,
            static,
            AudioCodecEnum.getAudioCodec(settingsManager.get().transcodeFormat),
            audioBitRate,
            settingsManager.get().playSessionId
        )

        return TranscodingAndMusicUrlData(audioBitRate, static, musicUrl)
    }

    /**
     * 清空播放列表
     */
    open fun clearPlayerList() {
        pause()
        downloadCacheController.cancelAllCache()
        _originMusicList.clear()
        _playMusicList.clear()
        musicCurrentPositionMap.clear()
        curOriginIndex = Constants.MINUS_ONE_INT
        musicInfo = null
        updateDuration(Constants.ZERO.toLong())
        setCurrentPositionData(Constants.ZERO.toLong())
        updateState(PlayStateEnum.None)
        headTime = Constants.ZERO.toLong()
        endTime = Constants.ZERO.toLong()
        setPageNumData(Constants.ZERO)
        pageSize = Constants.ZERO
    }

    override fun close() {
        pause()
        _originMusicList.clear()
        _playMusicList.clear()
        musicCurrentPositionMap.clear()
        curOriginIndex = Constants.MINUS_ONE_INT
        musicInfo = null
        updateDuration(Constants.ZERO.toLong())
        setCurrentPositionData(Constants.ZERO.toLong())
        updateState(PlayStateEnum.None)
        headTime = Constants.ZERO.toLong()
        endTime = Constants.ZERO.toLong()
        setPageNumData(Constants.ZERO)
        pageSize = Constants.ZERO
        super.close()
    }
}
