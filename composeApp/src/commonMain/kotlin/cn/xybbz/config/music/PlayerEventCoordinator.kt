package cn.xybbz.config.music

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.client.FavoriteCoordinator
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.config.scope.IoScoped
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.database.withTransaction
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.music.PlayHistoryMusic
import cn.xybbz.localdata.data.music.PlayQueueMusic
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.player.XyPlayer
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * 播放页推荐歌曲状态。
 */
data class PlayerRecommendationState(
    // 当前歌曲的相似歌曲列表
    val similarMusicList: List<XyMusic> = emptyList(),
    // 当前歌曲对应歌手的热门歌曲列表
    val popularMusicList: List<XyMusic> = emptyList()
)

/**
 * 播放器事件协调器。
 * 负责承接 musicController 发出的业务事件，并完成持久化、上报、补充数据加载等非 UI 工作。
 */
class PlayerEventCoordinator(
    private val db: LocalDatabaseClient,
    private val musicController: MusicCommonController,
    private val dataSourceManager: DataSourceManager,
    private val settingsManager: SettingsManager,
    private val musicPlayContext: MusicPlayContext,
    private val playbackProgressReporter: PlaybackProgressReporter
) : IoScoped() {

    // 推荐歌曲状态的唯一响应式来源
    private val _recommendationStateFlow = MutableStateFlow(PlayerRecommendationState())
    val recommendationStateFlow = _recommendationStateFlow.asStateFlow()

    /**
     * 当前歌曲的相似歌曲，供 UI 直接读取展示。
     */
    val similarMusicList: List<XyMusic>
        get() = recommendationStateFlow.value.similarMusicList

    /**
     * 当前歌曲所属歌手的热门歌曲，供 UI 直接读取展示。
     */
    val popularMusicList: List<XyMusic>
        get() = recommendationStateFlow.value.popularMusicList

    /**
     * 轻量切歌事件，供 UI 感知“歌曲已变化”。
     */
    private val _songChangeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val songChangeEvents = _songChangeEvents.asSharedFlow()

    // 播放器业务事件监听任务
    private var observeJob: Job? = null
    // 登录状态监听任务
    private var loginObserveJob: Job? = null
    // 专辑历史开关监听任务
    private var enableProgressJob: Job? = null
    // 当前数据源下允许记录进度的专辑配置
    private val enableProgressMap = mutableMapOf<String, Boolean>()

    init {
        createScope()
    }

    /**
     * 应用启动时调用一次，开始接管播放器事件。
     */
    fun start() {
        if (observeJob?.isActive == true) {
            return
        }

        observeJob = scope.launch {
            // 播放器业务事件统一在这里分发，避免 ViewModel 直接承担播放器协调职责。
            musicController.events.collect {
                when (it) {
                    is PlayerEvent.AddMusicList -> onAddMusicList(it.artistId, it.ifInitPlayerList)
                    PlayerEvent.BeforeChangeMusic -> onBeforeChangeMusic()
                    is PlayerEvent.ChangeMusic -> onChangeMusic(it.musicId, it.artistId, it.artistName)
                    is PlayerEvent.Favorite -> onFavoriteMusic(it.musicId)
                    is PlayerEvent.NextList -> onNextList()
                    is PlayerEvent.Pause -> onPause(it.musicId)
                    is PlayerEvent.Play -> onPlay(it.musicId)
                    is PlayerEvent.PlayerTypeChange -> onPlayerTypeChange(it.playerType)
                    is PlayerEvent.PositionSeekTo -> onPositionSeekTo(it.positionMs)
                    is PlayerEvent.RemovePlaybackProgress -> removePlaybackProgress(it.musicId)
                }
            }
        }

        loginObserveJob = scope.launch {
            dataSourceManager.loginStateFlow.collect {
                startEnableProgressObserver()
            }
        }
    }

    /**
     * 停止所有播放器相关监听，通常在应用退出或对象销毁时调用。
     */
    fun stop() {
        observeJob?.cancel()
        observeJob = null
        loginObserveJob?.cancel()
        loginObserveJob = null
        enableProgressJob?.cancel()
        enableProgressJob = null
        playbackProgressReporter.stop()
    }

    /**
     * 删除指定歌曲的播放进度记录。
     */
    private fun removePlaybackProgress(musicId: String) {
        scope.launch {
            db.progressDao.removeByMusicId(musicId)
        }
    }

    /**
     * 处理开始播放事件。
     */
    private fun onPlay(musicId: String) {
        if (settingsManager.get().ifEnableSyncPlayProgress) {
            scope.launch {
                dataSourceManager.reportPlaying(
                    musicId,
                    playSessionId = settingsManager.get().playSessionId,
                    positionTicks = musicController.progressStateFlow.value
                )
            }
            playbackProgressReporter.start()
        }
    }

    /**
     * 处理暂停播放事件。
     */
    private fun onPause(musicId: String) {
        playbackProgressReporter.stop()
        if (settingsManager.get().ifEnableSyncPlayProgress) {
            scope.launch {
                dataSourceManager.reportPlaying(
                    musicId,
                    playSessionId = settingsManager.get().playSessionId,
                    true,
                    musicController.progressStateFlow.value
                )
            }
        }
        setPlayerProgress(musicController.progressStateFlow.value)
    }

    /**
     * 处理拖动播放进度事件。
     */
    private fun onPositionSeekTo(millSeconds: Long) {
        setPlayerProgress(millSeconds)
        if (settingsManager.get().ifEnableSyncPlayProgress) {
            playbackProgressReporter.reportNow()
        }
    }

    /**
     * 切歌前先落库当前播放进度。
     */
    private fun onBeforeChangeMusic() {
        playbackProgressReporter.stop()
        setPlayerProgress(musicController.progressStateFlow.value)
    }

    /**
     * 处理收藏状态切换事件。
     */
    private fun onFavoriteMusic(musicId: String) {
        scope.launch {
            FavoriteCoordinator.setFavoriteData(
                dataSourceManager = dataSourceManager,
                type = MusicTypeEnum.MUSIC,
                itemId = musicId,
                ifFavorite = db.musicDao.selectIfFavoriteByMusic(musicId),
                musicController = musicController
            )
        }
    }

    /**
     * 处理切歌后的推荐数据刷新与历史记录持久化。
     */
    private fun onChangeMusic(musicId: String, artistId: String?, artistName: String?) {
        // 切歌后补充加载相似歌曲和热门歌曲，供播放页展示。
        scope.launch {
            val similar = dataSourceManager.getSimilarMusicList(musicId)
            _recommendationStateFlow.value = recommendationStateFlow.value.copy(
                similarMusicList = similar
            )
        }
        scope.launch {
            musicController.musicInfo?.let {
                val popular = dataSourceManager.getArtistPopularMusicList(
                    artistId ?: "",
                    artistName ?: ""
                )
                _recommendationStateFlow.value = recommendationStateFlow.value.copy(
                    popularMusicList = popular
                )
            }
        }
        scope.launch {
            // 通知 UI 层当前歌曲已切换，例如重置跑马灯等页面状态。
            _songChangeEvents.emit(Unit)
            db.withTransaction {
                db.musicDao.updateByPlayedCount(musicId)
                val recordMusic = db.musicDao.selectById(musicId)

                if (recordMusic != null) {
                    val index = db.musicDao.selectPlayHistoryIndexAsc() ?: -1
                    db.musicDao.savePlayHistoryMusic(
                        listOf(
                            PlayHistoryMusic(
                                musicId = musicId,
                                index = index - 1,
                                connectionId = dataSourceManager.getConnectionId()
                            )
                        )
                    )
                }
                val recordCount = db.musicDao.selectPlayHistoryCount()
                if (recordCount > 20) {
                    db.musicDao.deletePlayHistory()
                }
                db.playerDao.updateIndex(musicId)
            }
        }
    }

    /**
     * 监听当前数据源的专辑进度记录开关。
     */
    private fun startEnableProgressObserver() {
        enableProgressJob?.cancel()

        enableProgressJob = scope.launch {
            // 登录源变化后重新订阅当前数据源下的专辑播放历史配置。
            db.enableProgressDao
                .getAlbumEnableProgressMap()
                .distinctUntilChanged()
                .collect { map ->
                    enableProgressMap.clear()
                    enableProgressMap.putAll(map)
                }
        }
    }

    /**
     * 持久化当前播放模式。
     */
    private fun onPlayerTypeChange(playerModeEnum: PlayerModeEnum) {
        scope.launch {
            val player = db.playerDao.selectPlayerByDataSource()
            if (player != null) {
                db.playerDao.updatePlayType(playerModeEnum)
            } else {
                db.playerDao.save(
                    XyPlayer(
                        playerType = playerModeEnum,
                        connectionId = dataSourceManager.getConnectionId(),
                        dataType = musicController.playDataType,
                        pageSize = 0
                    )
                )
            }
        }
    }

    /**
     * 同步新增播放列表到本地播放队列。
     */
    private fun onAddMusicList(artistId: String?, ifInitPlayerList: Boolean) {
        if (ifInitPlayerList) {
            return
        }

        scope.launch {
            // 这里只持久化“运行中新增/变更后的队列”，初始化恢复场景不重复回写。
            var index = db.musicDao.selectPlayQueueIndex() ?: -1
            val xyMusicList = musicController.originMusicList.map {
                index += 1
                PlayQueueMusic(
                    musicId = it.itemId,
                    index = index,
                    connectionId = dataSourceManager.getConnectionId()
                )
            }
            if (xyMusicList.isNotEmpty()) {
                db.musicDao.removeByType(
                    dataType = MusicDataTypeEnum.PLAY_QUEUE,
                    ifRemoveMusic = false
                )
                db.musicDao.savePlayQueueMusic(xyMusicList)

                val player = db.playerDao.selectPlayerByDataSource()
                val xyPlayer = XyPlayer(
                    connectionId = dataSourceManager.getConnectionId(),
                    dataType = musicController.playDataType,
                    musicId = musicController.musicInfo?.itemId ?: "",
                    headTime = musicController.headTime,
                    endTime = musicController.endTime,
                    playerType = musicController.playMode,
                    pageNum = musicController.pageNum,
                    ifSkip = musicController.headTime > 0,
                    albumId = musicController.musicInfo?.album ?: "",
                    artistId = artistId,
                    pageSize = musicController.pageSize
                )
                if (player != null) {
                    db.playerDao.updateById(
                        xyPlayer.copy(
                            id = player.id,
                            artistId = player.artistId
                        )
                    )
                } else {
                    db.playerDao.save(xyPlayer)
                }
            }
        }
    }

    /**
     * 加载下一页播放列表数据。
     */
    private fun onNextList() {
        // 下一页加载状态是全局播放流程的一部分，因此也放在协调器里维护。
        musicPlayContext.updateIfNextPageNumList(true)
        scope.launch {
            val newPageNum = musicController.pageNum + 1
            val newMusicList = musicPlayContext.musicPlayData?.onNextMusicList?.invoke(newPageNum)
            if (!newMusicList.isNullOrEmpty()) {
                musicController.setPageNumData(newPageNum)
                musicController.addMusicList(
                    newMusicList,
                    musicPlayContext.musicPlayData?.onMusicPlayParameter?.artistId
                )
                musicController.updateRestartCount()
            } else {
                musicController.updateIfGetNextPageMusicDataIsNullCount(1)
            }
        }.invokeOnCompletion { musicPlayContext.updateIfNextPageNumList(false) }
    }

    /**
     * 持久化当前歌曲的播放进度。
     */
    private fun setPlayerProgress(progress: Long) {
        scope.launch {
            musicController.musicInfo?.let {
                if (
                    settingsManager.get().ifEnableAlbumHistory ||
                        (enableProgressMap.containsKey(it.album) && enableProgressMap[it.album] == true)
                ) {
                    // 专辑历史开启后，暂停、切歌、拖动时都需要把当前位置持久化。
                    savePlayerProgress(
                        progress = progress,
                        progressPercentage = calculateProgressPercentage(
                            progress,
                            musicController.duration
                        ),
                        musicId = it.itemId,
                        musicName = it.name,
                        index = musicController.curOriginIndex,
                        albumId = it.album
                    )
                }
            }
        }
    }

    /**
     * 根据时长计算百分比进度，使用四舍六入五成双避免累计偏差。
     */
    private fun calculateProgressPercentage(progress: Long, duration: Long): Int {
        if (duration <= 0L) {
            return 0
        }

        val scaledProgress = progress * 100L
        val quotient = scaledProgress / duration
        val remainder = scaledProgress % duration
        val doubledRemainder = remainder * 2L

        val roundedPercentage = when {
            doubledRemainder < duration -> quotient
            doubledRemainder > duration -> quotient + 1
            quotient % 2L == 0L -> quotient
            else -> quotient + 1
        }

        return roundedPercentage.toInt()
    }

    /**
     * 保存播放进度到数据库。
     */
    private suspend fun savePlayerProgress(
        progress: Long,
        progressPercentage: Int,
        musicId: String,
        musicName: String,
        index: Int,
        albumId: String?
    ) {
        db.progressDao.save(
            cn.xybbz.localdata.data.progress.Progress(
                musicId = musicId,
                albumId = albumId ?: "",
                progress = progress,
                progressPercentage = progressPercentage,
                index = index,
                musicName = musicName,
                connectionId = dataSourceManager.getConnectionId()
            )
        )
    }

    override fun close() {
        stop()
        super.close()
    }
}
