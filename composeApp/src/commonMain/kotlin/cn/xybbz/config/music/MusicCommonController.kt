package cn.xybbz.config.music

import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.config.scope.IoScoped
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

abstract class MusicCommonController : IoScoped(), KoinComponent {

    // 下载缓存控制器
    val downloadCacheController: DownloadCacheCommonController = get()

    // 原始歌曲列表的唯一响应式来源
    private val _originMusicListFlow = MutableStateFlow<List<XyPlayMusic>>(emptyList())
    val originMusicListFlow = _originMusicListFlow.asStateFlow()
    // 原始歌曲列表的便捷读取入口
    val originMusicList: List<XyPlayMusic>
        get() = originMusicListFlow.value

    // 当前播放模式下的播放列表唯一响应式来源 todo 应该存放拼接后的连接
    private val _playMusicListFlow = MutableStateFlow<List<XyPlayMusic>>(emptyList())
    val playMusicListFlow = _playMusicListFlow.asStateFlow()
    // 当前播放模式下播放列表的便捷读取入口
    val playMusicList: List<XyPlayMusic>
        get() = playMusicListFlow.value

    // 当前播放歌曲进度映射的唯一响应式来源
    private val _musicCurrentPositionMapFlow = MutableStateFlow<Map<String, Long>>(emptyMap())
    val musicCurrentPositionMapFlow = _musicCurrentPositionMapFlow.asStateFlow()
    // 当前播放歌曲进度映射的便捷读取入口
    val musicCurrentPositionMap: Map<String, Long>
        get() = musicCurrentPositionMapFlow.value

    // 播放器主状态的唯一响应式来源
    private val _playbackStateFlow = MutableStateFlow(PlaybackState())
    val playbackStateFlow = _playbackStateFlow.asStateFlow()

    // 当前播放歌曲在原始列表中的索引
    val curOriginIndex: Int
        get() = playbackStateFlow.value.curOriginIndex

    // 当前播放歌曲在实际播放列表中的索引
    val curRealIndex: Int
        get() = playbackStateFlow.value.curRealIndex

    // 当前分页页码
    val pageNum: Int
        get() = playbackStateFlow.value.pageNum

    // 当前分页大小
    val pageSize: Int
        get() = playbackStateFlow.value.pageSize

    // 当前播放歌曲信息
    val musicInfo: XyPlayMusic?
        get() = playbackStateFlow.value.musicInfo

    // 当前歌曲封面字节流
    val picByte: ByteArray?
        get() = playbackStateFlow.value.picByte

    // 当前封面刷新版本号
    val coverRefreshVersion: Int
        get() = playbackStateFlow.value.coverRefreshVersion

    // 当前歌曲总时长
    val duration: Long
        get() = playbackStateFlow.value.duration

    // 当前播放器状态
    val state: PlayStateEnum
        get() = playbackStateFlow.value.state

    // 播放进度
    private val _progressStateFlow = MutableStateFlow(0L)
    val progressStateFlow = _progressStateFlow.asStateFlow()

    // 当前播放数据类型
    val playDataType: MusicPlayTypeEnum
        get() = playbackStateFlow.value.playDataType

    // 片头跳过时间
    val headTime: Long
        get() = playbackStateFlow.value.headTime

    // 片尾跳过时间
    val endTime: Long
        get() = playbackStateFlow.value.endTime

    // 当前播放模式
    val playMode: PlayerModeEnum
        get() = playbackStateFlow.value.playMode

    // 是否能加载下一页
    var ifNextPage = true
        private set

    // 加载下一页数据为空次数
    var ifGetNextPageMusicDataIsNullCount: Int = 0

    /**
     * 是否能操作 playMusicList
     */
    open val isPlayMusicListMutable: Boolean = false

    // 播放器业务事件流
    private val _events = MutableSharedFlow<PlayerEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val events = _events.asSharedFlow()

    init {
        createScope()
    }

    /**
     * 初始化播放
     */
    abstract fun initController(onRestorePlaylists: (MusicCommonController.() -> Unit)? = null)

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
     * 跳转到下一首
     */
    abstract fun seekToNext()

    /**
     * 跳转到上一首
     */
    abstract fun seekToPrevious()

    /**
     * 跳转至指定 index 位置音乐
     */
    abstract fun seekToIndex(index: Int)

    /**
     * 根据音乐 id 跳转
     */
    abstract fun seekToItemId(itemId: String)

    /**
     * 删除指定 index 位置音乐
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
        updatePlaybackState { it.copy(playMode = playerModeEnum) }
        updateEvent(PlayerEvent.PlayerTypeChange(playerModeEnum))
        updatePlayerMode()
        if (isPlayMusicListMutable) {
            insertPlayMusicList(originMusicList)
        }
    }

    /**
     * 添加下一首播放项。
     */
    abstract fun addNextPlayer(music: XyPlayMusic)

    /**
     * 获取下一首播放位置的索引
     */
    open fun getNextPlayableIndex(): Int? {
        return if (isPlayMusicListMutable) {
            if (playMusicList.isEmpty() || curRealIndex !in playMusicList.indices) {
                null
            } else {
                val nextIndex = if (curRealIndex == playMusicList.lastIndex) 0 else curRealIndex + 1
                nextIndex.takeIf { it in playMusicList.indices }
            }
        } else {
            if (originMusicList.isEmpty() || curOriginIndex !in originMusicList.indices) {
                null
            } else {
                val nextIndex =
                    if (curOriginIndex == originMusicList.lastIndex) 0 else curOriginIndex + 1
                nextIndex.takeIf { it in originMusicList.indices }
            }
        }
    }

    /**
     * 获取上一首播放位置的索引
     */
    open fun getPreviousPlayableIndex(): Int? {
        return if (isPlayMusicListMutable) {
            if (playMusicList.isEmpty() || curRealIndex !in playMusicList.indices) {
                null
            } else {
                val previousIndex = if (curRealIndex == 0) playMusicList.lastIndex else curRealIndex - 1
                previousIndex.takeIf { it in playMusicList.indices }
            }
        } else {
            if (originMusicList.isEmpty() || curOriginIndex !in originMusicList.indices) {
                null
            } else {
                val previousIndex =
                    if (curOriginIndex == 0) originMusicList.lastIndex else curOriginIndex - 1
                previousIndex.takeIf { it in originMusicList.indices }
            }
        }
    }

    /**
     * 替换音乐播放连接
     */
    abstract fun replacePlaylistItemUrl(updateMusicUrlFun: (XyPlayMusic) -> XyPlayMusic)

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
        updatePlaybackState {
            it.copy(headTime = headTime, endTime = endTime)
        }
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
        replaceMusicCurrentPositionMap(musicCurrentPositionMapData ?: emptyMap())
        replacePlaylist(musicDataList)
        setPageNumData(pageNum)
        updatePageSize(pageSize)
    }

    /**
     * 刷新播放列表封面相关元数据。
     */
    abstract fun refreshPlaylistCoverMetadata()

    /**
     * 各播放器自行处理播放模式切换后的底层逻辑。
     */
    protected abstract fun updatePlayerMode()

    /**
     * 开始缓存
     */
    fun startCache(music: XyPlayMusic, ifStatic: Boolean) {
        if (music.filePath.isNullOrBlank()) {
            downloadCacheController.cacheMedia(music, ifStatic)
        }
    }

    /**
     * 设置当前播放进度
     */
    fun setCurrentPositionData(currentPosition: Long) {
        _progressStateFlow.value = currentPosition
    }

    /**
     * 调用 onFavorite
     */
    fun invokingOnFavorite(itemId: String) {
        updateEvent(PlayerEvent.Favorite(itemId))
    }

    /**
     * 更新播放器状态。
     */
    fun updateState(state: PlayStateEnum) {
        updatePlaybackState { it.copy(state = state) }
    }

    /**
     * 更新当前歌曲总时长。
     */
    fun updateDuration(duration: Long) {
        updatePlaybackState { it.copy(duration = duration) }
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

    /**
     * 更新是否可以加载下一页音乐数据
     */
    fun updateRestartCount() {
        this.ifGetNextPageMusicDataIsNullCount = 0
        updateIfNextPage(true)
    }

    /**
     * 更新是否允许继续加载下一页。
     */
    private fun updateIfNextPage(ifNextPage: Boolean) {
        this.ifNextPage = ifNextPage
    }

    /**
     * 发送播放器业务事件。
     */
    fun updateEvent(event: PlayerEvent) {
        scope.launch {
            _events.emit(event)
        }
    }

    /**
     * 设置 PageNum
     */
    fun setPageNumData(pageNum: Int) {
        updatePlaybackState { it.copy(pageNum = pageNum) }
    }

    /**
     * 上报“开始播放”事件。
     */
    fun reportedPlayEvent() {
        musicInfo?.let {
            updateEvent(PlayerEvent.Play(it.itemId))
        }
    }

    /**
     * 上报“暂停播放”事件。
     */
    fun reportedPauseEvent() {
        musicInfo?.let {
            scope.launch {
                _events.emit(PlayerEvent.Pause(it.itemId))
            }
        }
    }

    /**
     * 更新列表数据
     */
    fun replacePlaylist(musicList: List<XyPlayMusic>) {
        _originMusicListFlow.value = musicList.toList()
        if (isPlayMusicListMutable) {
            insertPlayMusicList(musicList)
        }
    }

    /**
     * 在原始播放列表尾部追加单首歌曲。
     */
    fun addMusic(music: XyPlayMusic) {
        _originMusicListFlow.update { it + music }
        if (isPlayMusicListMutable) {
            _playMusicListFlow.update { it + music }
        }
    }

    fun insertMusic(music: XyPlayMusic) {
        val insertIndex = (curOriginIndex + 1).coerceAtLeast(0)
        _originMusicListFlow.update { list ->
            list.toMutableList().apply {
                add(insertIndex.coerceAtMost(size), music)
            }
        }
        if (isPlayMusicListMutable) {
            val playIndex = (curRealIndex + 1).coerceAtLeast(0)
            _playMusicListFlow.update { list ->
                list.toMutableList().apply {
                    add(playIndex.coerceAtMost(size), music)
                }
            }
        }
    }

    /**
     * 在当前播放位置后批量插入歌曲。
     */
    fun addMusicList(musicList: List<XyPlayMusic>) {
        val insertIndex = (curOriginIndex + 1).coerceAtLeast(0)
        _originMusicListFlow.update { list ->
            list.toMutableList().apply {
                addAll(insertIndex.coerceAtMost(size), musicList)
            }
        }
        if (isPlayMusicListMutable) {
            val playIndex = (curRealIndex + 1).coerceAtLeast(0)
            _playMusicListFlow.update { list ->
                list.toMutableList().apply {
                    addAll(playIndex.coerceAtMost(size), musicList)
                }
            }
        }
    }

    /**
     * 删除指定索引位置的数据
     */
    fun removeMusic(index: Int) {
        if (index !in originMusicList.indices) {
            return
        }
        val playMusic = originMusicList[index]
        _originMusicListFlow.update { list ->
            list.toMutableList().apply {
                removeAt(index)
            }
        }
        if (isPlayMusicListMutable) {
            _playMusicListFlow.update { list ->
                list.toMutableList().apply {
                    remove(playMusic)
                }
            }
        }
    }

    /**
     * 更新播放音乐的原始索引
     */
    fun updateOriginIndex(originIndex: Int) {
        val currentMusic = originMusicList.getOrNull(originIndex) ?: return
        val newRealIndex = if (isPlayMusicListMutable) {
            playMusicList.indexOfFirst { it.itemId == currentMusic.itemId }
        } else {
            curRealIndex
        }
        updatePlaybackState {
            it.copy(
                curOriginIndex = originIndex,
                curRealIndex = newRealIndex
            )
        }
        syncCurrentMusicAfterOriginIndexChanged()
    }

    /**
     * 更新播放索引
     */
    fun updateRealIndex(realIndex: Int) {
        val currentMusic = playMusicList.getOrNull(realIndex) ?: return
        val newOriginIndex = if (isPlayMusicListMutable) {
            originMusicList.indexOfFirst { it.itemId == currentMusic.itemId }
        } else {
            curOriginIndex
        }
        updatePlaybackState {
            it.copy(
                curRealIndex = realIndex,
                curOriginIndex = newOriginIndex
            )
        }
        syncCurrentMusicAfterOriginIndexChanged()
    }

    /**
     * 根据播放模式重建当前播放列表。
     */
    private fun insertPlayMusicList(musicList: List<XyPlayMusic>) {
        _playMusicListFlow.value = when (playMode) {
            PlayerModeEnum.SINGLE_LOOP,
            PlayerModeEnum.SEQUENTIAL_PLAYBACK -> musicList.toList()

            PlayerModeEnum.RANDOM_PLAY -> musicList.shuffled()
        }
    }

    /**
     * 上报加载下一页事件，更新当前播放音乐信息，更新当前音乐的时长
     */
    private fun syncCurrentMusicAfterOriginIndexChanged() {
        if (originMusicList.isEmpty() || curOriginIndex !in originMusicList.indices) {
            return
        }
        if (curOriginIndex >= originMusicList.size - 1 && ifNextPage) {
            updateEvent(PlayerEvent.NextList(pageNum))
        }
        val music = originMusicList[curOriginIndex]
        updateCurrentMusic(music)
        updateDuration(music.runTimeTicks)
        updateEvent(
            PlayerEvent.ChangeMusic(
                music.itemId,
                music.artistIds?.firstOrNull(),
                music.artists?.firstOrNull()
            )
        )
    }

    /**
     * 更新 coverRefreshVersion 版本号
     */
    fun updateCoverRefreshVersion(version: Int) {
        updatePlaybackState {
            it.copy(coverRefreshVersion = it.coverRefreshVersion + version)
        }
    }

    /**
     * 更新当前播放音乐
     */
    protected fun updateCurrentMusic(music: XyPlayMusic?) {
        updatePlaybackState { it.copy(musicInfo = music) }
    }

    /**
     * 更新当前页面字节码
     */
    fun updatePicBytes(picBytes: ByteArray?) {
        updatePlaybackState { it.copy(picByte = picBytes) }
    }

    /**
     * 更新分页大小
     */
    fun updatePageSize(pageSize: Int) {
        updatePlaybackState { it.copy(pageSize = pageSize) }
    }

    /**
     * 更新当前播放数据类型
     */
    open fun updatePlayDataType(playDataType: MusicPlayTypeEnum) {
        updatePlaybackState { it.copy(playDataType = playDataType) }
    }

    /**
     * 整体替换歌曲进度映射。
     */
    fun replaceMusicCurrentPositionMap(map: Map<String, Long>) {
        _musicCurrentPositionMapFlow.value = map.toMap()
    }

    /**
     * 更新单首歌曲的播放进度。
     */
    fun putMusicCurrentPosition(musicId: String, position: Long) {
        _musicCurrentPositionMapFlow.update {
            it + (musicId to position)
        }
    }

    /**
     * 删除单首歌曲的播放进度记录。
     */
    fun removeMusicCurrentPosition(musicId: String) {
        _musicCurrentPositionMapFlow.update {
            it - musicId
        }
    }

    /**
     * 清空播放列表
     */
    open fun clearPlayerList() {
        pause()
        downloadCacheController.cancelAllCache()
        _originMusicListFlow.value = emptyList()
        _playMusicListFlow.value = emptyList()
        replaceMusicCurrentPositionMap(emptyMap())
        updatePlaybackState {
            it.copy(
                curOriginIndex = Constants.MINUS_ONE_INT,
                curRealIndex = Constants.MINUS_ONE_INT,
                musicInfo = null,
                duration = Constants.ZERO.toLong(),
                state = PlayStateEnum.None,
                headTime = Constants.ZERO.toLong(),
                endTime = Constants.ZERO.toLong(),
                pageNum = Constants.ZERO,
                pageSize = Constants.ZERO
            )
        }
        setCurrentPositionData(Constants.ZERO.toLong())
    }

    override fun close() {
        pause()
        _originMusicListFlow.value = emptyList()
        _playMusicListFlow.value = emptyList()
        replaceMusicCurrentPositionMap(emptyMap())
        updatePlaybackState {
            it.copy(
                curOriginIndex = Constants.MINUS_ONE_INT,
                curRealIndex = Constants.MINUS_ONE_INT,
                musicInfo = null,
                duration = Constants.ZERO.toLong(),
                state = PlayStateEnum.None,
                headTime = Constants.ZERO.toLong(),
                endTime = Constants.ZERO.toLong(),
                pageNum = Constants.ZERO,
                pageSize = Constants.ZERO
            )
        }
        setCurrentPositionData(Constants.ZERO.toLong())
        super.close()
    }

    /**
     * 统一更新播放器主状态，避免外部散落写入多个字段。
     */
    private inline fun updatePlaybackState(transform: (PlaybackState) -> PlaybackState) {
        _playbackStateFlow.update(transform)
    }
}
