package cn.xybbz.config.music

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

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

    /**
     * 当前歌曲的相似歌曲，供 UI 直接读取展示。
     */
    var similarMusicList by mutableStateOf(emptyList<XyMusic>())
        private set

    /**
     * 当前歌曲所属歌手的热门歌曲，供 UI 直接读取展示。
     */
    var popularMusicList by mutableStateOf(emptyList<XyMusic>())
        private set

    /**
     * 每次切歌递增一次，供 UI 层感知“歌曲已变化”这种轻量事件。
     */
    var songChangeVersion by mutableIntStateOf(0)
        private set

    private var observeJob: Job? = null
    private var loginObserveJob: Job? = null
    private var enableProgressJob: Job? = null
    private val enableProgressMap = mutableStateMapOf<String, Boolean>()

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

        // 播放器业务事件统一在这里分发，避免 ViewModel 直接承担播放器协调职责。
        observeJob = scope.launch {
            musicController.events.collect {
                when (it) {
                    is PlayerEvent.AddMusicList -> {
                        onAddMusicList(it.artistId, it.ifInitPlayerList)
                    }

                    PlayerEvent.BeforeChangeMusic -> {
                        onBeforeChangeMusic()
                    }

                    is PlayerEvent.ChangeMusic -> {
                        onChangeMusic(it.musicId, it.artistId, it.artistName)
                    }

                    is PlayerEvent.Favorite -> {
                        onFavoriteMusic(it.musicId)
                    }

                    is PlayerEvent.NextList -> {
                        onNextList()
                    }

                    is PlayerEvent.Pause -> {
                        onPause(it.musicId, it.playSessionId)
                    }

                    is PlayerEvent.Play -> {
                        onPlay(it.musicId, it.playSessionId)
                    }

                    is PlayerEvent.PlayerTypeChange -> {
                        onPlayerTypeChange(it.playerType)
                    }

                    is PlayerEvent.PositionSeekTo -> {
                        onPositionSeekTo(it.positionMs)
                    }

                    is PlayerEvent.RemovePlaybackProgress -> {
                        removePlaybackProgress(it.musicId)
                    }
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

    private fun removePlaybackProgress(musicId: String) {
        scope.launch {
            db.progressDao.removeByMusicId(musicId)
        }
    }

    private fun onPlay(musicId: String, playSessionId: String) {
        if (settingsManager.get().ifEnableSyncPlayProgress) {
            scope.launch {
                dataSourceManager.reportPlaying(
                    musicId,
                    playSessionId = playSessionId,
                    positionTicks = musicController.progressStateFlow.value
                )
            }
            playbackProgressReporter.start()
        }
    }

    private fun onPause(musicId: String, playSessionId: String) {
        playbackProgressReporter.stop()
        if (settingsManager.get().ifEnableSyncPlayProgress) {
            scope.launch {
                dataSourceManager.reportPlaying(
                    musicId,
                    playSessionId = playSessionId,
                    true,
                    musicController.progressStateFlow.value
                )
            }
        }
        setPlayerProgress(musicController.progressStateFlow.value)
    }

    private fun onPositionSeekTo(millSeconds: Long) {
        setPlayerProgress(millSeconds)
        if (settingsManager.get().ifEnableSyncPlayProgress) {
            playbackProgressReporter.reportNow()
        }
    }

    private fun onBeforeChangeMusic() {
        playbackProgressReporter.stop()
        setPlayerProgress(musicController.progressStateFlow.value)
    }

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

    private fun onChangeMusic(musicId: String, artistId: String?, artistName: String?) {
        // 切歌后补充加载相似歌曲和热门歌曲，供播放页展示。
        scope.launch {
            similarMusicList = dataSourceManager.getSimilarMusicList(musicId)
        }
        scope.launch {
            musicController.musicInfo?.let {
                popularMusicList =
                    dataSourceManager.getArtistPopularMusicList(
                        artistId ?: "",
                        artistName ?: ""
                    )
            }
        }
        scope.launch {
            // 通知 UI 层当前歌曲已切换，例如重置跑马灯等页面状态。
            songChangeVersion += 1
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

    private fun startEnableProgressObserver() {
        enableProgressJob?.cancel()

        enableProgressJob = scope.launch {
            // 登录源变化后重新订阅当前数据源下的专辑播放历史配置。
            db.enableProgressDao
                .getAlbumEnableProgressMap()
                .distinctUntilChanged()
                .collect { map ->
                    if (map.isNotEmpty()) {
                        enableProgressMap.clear()
                        enableProgressMap.putAll(map)
                    }
                }
        }
    }

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

    private fun onNextList() {
        // 下一页加载状态是全局播放流程的一部分，因此也放在协调器里维护。
        ifNextPageNumList = true
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
        }.invokeOnCompletion { ifNextPageNumList = false }
    }

    private fun setPlayerProgress(progress: Long) {
        scope.launch {
            musicController.musicInfo?.let {
                // 专辑历史开启后，暂停/切歌/拖动时都需要把当前位置持久化。
                if (settingsManager.get().ifEnableAlbumHistory || (
                        enableProgressMap.containsKey(it.album) &&
                            enableProgressMap[it.album] == true
                        )
                ) {
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
