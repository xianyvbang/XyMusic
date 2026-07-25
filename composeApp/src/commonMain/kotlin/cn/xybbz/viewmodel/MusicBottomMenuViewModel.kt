/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.assembler.MusicPlayAssembler
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.DownloadTypes
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.download.enqueueMusicDownload
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.music.PlayerEvent
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.volume.VolumeServer
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.setting.SkipTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.KoinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.cancel_timer_close_message
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@KoinViewModel
class MusicBottomMenuViewModel(
    private val settingsManager: SettingsManager,
    private val db: LocalDatabaseClient,
    private val downloadDb: DownloadDatabaseClient,
    val musicController: MusicCommonController,
    private val musicPlayContext: MusicPlayContext,
    val dataSourceManager: DataSourceManager,
    val volumeServer: VolumeServer,
    val downloaderManager: DownloaderManager
) : ViewModel() {

    val downloadMusicIdsFlow =
        downloadDb.downloadDao.getAllMusicTaskUidsFlow(
            notTypeData = DownloadTypes.APK.toString(),
            mediaLibraryId = settingsManager.get().connectionId.toString()
        )
    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    var volumeValue by mutableStateOf(0f)
        private set

    /**
     * 播放速度
     */
    var doubleSpeed by mutableFloatStateOf(1f)
        private set

    /**
     * 是否播放完成后关闭
     */
    var ifPlayEndClose by mutableStateOf(false)
        private set

    /**
     * 艺术家列表
     */
    var xyArtists by mutableStateOf<List<XyArtist>>(emptyList())
        private set

    /** 记录自然结束事件的递增序号，避免定时器复用旧的结束事件。 */
    private var trackEndSequence = 0L

    /** 缓冲最近的自然结束事件，避免等待任务建立时丢失边界事件。 */
    private val trackEndSignals = MutableSharedFlow<TrackEndSignal>(
        replay = 1,
        extraBufferCapacity = 16
    )

    /** 持续接收播放器产生的自然结束事件。 */
    private var trackEndObserverJob: Job? = null

    init {
        getDoubleSpeed()
        refreshVolume()
        trackEndObserverJob = observeTrackEndEvents()
    }

    //region 定时关闭
    /**
     * 定时信息
     */
    var sliderTimerEndData by mutableFloatStateOf(0f)
        private set

    fun setSliderTimerEndDataValue(value: Float) {
        sliderTimerEndData = value
    }

    //真正定时时间(分钟)
    var timerInfo by mutableLongStateOf(0L)
        private set

    fun setTimerInfoData(value: Long) {
        timerInfo = value
    }

    private var timerJob: Job? = null
    private var stopAfterCurrentTrackJob: Job? = null

    //endregion
    //region 播放速度
    private fun getDoubleSpeed() {
        viewModelScope.launch {
            db.settingsDao.selectDoubleSpeed().distinctUntilChanged().collect {
                if (it != null)
                    doubleSpeed = it
            }
        }

    }
    //endregion


    /**
     * 存储倍速播放
     */
    suspend fun setDoubleSpeed(value: Float) {
        settingsManager.saveOrUpdateDoubleSpeed(value)
    }

    /**
     * 保存专辑跳过片头片尾
     */
    suspend fun saveOrUpdateSkipTimeData(skipTime: SkipTime) {
        skipTime.connectionId = dataSourceManager.getConnectionId()
        if (skipTime.id != 0L) {
            db.skipTimeDao.updateByID(skipTime)
        } else {
            db.skipTimeDao.save(skipTime)
        }

        musicController.setHeadAndEntTime(
            headTime = skipTime.headTime,
            endTime = skipTime.endTime
        )
    }

    /**
     * 根据专辑id获得跳过片头和片尾
     */
    suspend fun getSkipTimeData(albumId: String): SkipTime {
        val selectByAlbumId = db.skipTimeDao.selectByAlbumId(albumId)
        return selectByAlbumId ?: SkipTime(connectionId = 0)
    }


    /**
     * 删除选中数据
     */
    suspend fun removeMusicResource(musicData: XyMusic) {
        dataSourceManager.removeMusicById(musicData.itemId)
    }

    /**
     * 音乐app定时关闭
     */
    @OptIn(FormatStringsInDatetimeFormats::class)
    fun createMusicStop(timerClose: String) {
        cancelTimerJobs()
        if (timerInfo <= 0L || sliderTimerEndData <= 0f) {
            return
        }

        val systemTimeZone = TimeZone.currentSystemDefault()
        val targetTime = (Clock.System.now() + timerInfo.toInt().minutes)
            .toLocalDateTime(systemTimeZone)
        val timeText = LocalDateTime.Format {
            byUnicodePattern("HH:mm:ss")
        }.format(targetTime)

        timerJob = viewModelScope.launch {
            delay((timerInfo * 60_000L).milliseconds)
            timerJob = null

            val currentItemId = musicController.musicInfo?.itemId
            val shouldWaitForTrackEnd = ifPlayEndClose &&
                    !currentItemId.isNullOrBlank() &&
                    (musicController.state == PlayStateEnum.Playing || musicController.state == PlayStateEnum.Loading)

            if (shouldWaitForTrackEnd) {
                waitForCurrentTrackToFinish(currentItemId)
            } else {
                stopPlaybackByTimer()
            }
        }

        MessageUtils.sendPopTip("$timeText$timerClose")
    }

    /**
     * 取消音乐app定时关闭
     */
    fun cancelAlarm() {
        cancelTimerJobs()
        MessageUtils.sendPopTip(
            Res.string.cancel_timer_close_message
        )
    }

    /**
     * 设置播放完关闭
     */
    fun setPlayEndCloseData(data: Boolean) {
        ifPlayEndClose = data
    }

    /**
     * 获得艺术家信息
     */
    fun getArtistInfos(artists: List<String>) {
        viewModelScope.launch {
            try {
                xyArtists =
                    dataSourceManager.selectArtistInfoByIds(artists)
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "获得艺术家信息失败", e)
            }

        }
    }

    fun downloadMusic(musicData: XyMusic) {
        viewModelScope.launch {
            downloaderManager.enqueueMusicDownload(musicData, dataSourceManager)
        }

    }

    fun addNextPlayer(itemId: String) {
        viewModelScope.launch {
            val playMusic = MusicPlayAssembler.attachFilePath(
                playMusic = db.musicDao.selectExtendById(itemId),
                downloadDb = downloadDb,
                mediaLibraryId = dataSourceManager.getConnectionId().toString()
            )
            playMusic?.let {
                musicPlayContext.addNextPlayer(it)
            }
        }

    }

    fun refreshVolume() {
        this.volumeValue = (volumeServer.getStreamVolume().toFloat() / volumeServer.getMaxVolume())
    }

    fun updateVolume(value: Float) {
        viewModelScope.launch {
            volumeServer.updateVolume((value * 100).toInt())
            this@MusicBottomMenuViewModel.volumeValue = value
        }
    }

    fun setFadeDurationMs(fadeDurationMs: Long) {
        viewModelScope.launch {
            settingsManager.setFadeDurationMs(fadeDurationMs)
        }
    }

    fun getFadeDurationMs(): Long {
        return settingsManager.get().fadeDurationMs
    }

    override fun onCleared() {
        cancelTimerJobs()
        trackEndObserverJob?.cancel()
        trackEndObserverJob = null
        super.onCleared()
    }

    private fun cancelTimerJobs() {
        timerJob?.cancel()
        timerJob = null
        stopAfterCurrentTrackJob?.cancel()
        stopAfterCurrentTrackJob = null
    }

    /** 持续收集播放器自然结束事件，并为每个事件分配递增序号。 */
    private fun observeTrackEndEvents(): Job {
        return viewModelScope.launch {
            musicController.events
                .filterIsInstance<PlayerEvent.RemovePlaybackProgress>()
                .collect { event ->
                    trackEndSequence += 1L
                    trackEndSignals.emit(
                        TrackEndSignal(
                            sequence = trackEndSequence,
                            musicId = event.musicId
                        )
                    )
                }
        }
    }

    private fun waitForCurrentTrackToFinish(currentItemId: String) {
        stopAfterCurrentTrackJob?.cancel()
        // 仅接收定时器到期后产生的自然结束事件，避免重放旧信号。
        val baselineTrackEndSequence = trackEndSequence
        stopAfterCurrentTrackJob = viewModelScope.launch {
            merge(
                trackEndSignals
                    .filter {
                        isNewMatchingTrackEndSignal(
                            signalSequence = it.sequence,
                            baselineSequence = baselineTrackEndSequence,
                            waitingMusicId = currentItemId,
                            endedMusicId = it.musicId
                        )
                    }
                    .map {
                        MusicTimerStopCandidate(
                            currentMusicId = musicController.musicInfo?.itemId,
                            playbackState = musicController.state,
                            playerEvent = PlayerEvent.RemovePlaybackProgress(it.musicId)
                        )
                    },
                musicController.musicInfoFlow
                    .filter { it?.itemId != currentItemId }
                    .map {
                        MusicTimerStopCandidate(
                            currentMusicId = it?.itemId,
                            playbackState = musicController.state
                        )
                    },
                musicController.stateFlow
                    .filter { it == PlayStateEnum.Pause || it == PlayStateEnum.None }
                    .map {
                        MusicTimerStopCandidate(
                            currentMusicId = musicController.musicInfo?.itemId,
                            playbackState = it
                        )
                    }
            )
                .mapNotNull { candidate ->
                    resolveMusicTimerStopReason(
                        waitingMusicId = currentItemId,
                        currentMusicId = candidate.currentMusicId,
                        playbackState = candidate.playbackState,
                        playerEvent = candidate.playerEvent
                    )
                }
                .first()

            stopPlaybackByTimer()
            musicController.clearPlayerList()
        }
    }

    private fun stopPlaybackByTimer() {
        stopAfterCurrentTrackJob?.cancel()
        stopAfterCurrentTrackJob = null
        timerInfo = 0L
        sliderTimerEndData = 0f
        musicController.pause()
    }

}

