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
import cn.xybbz.api.client.FavoriteCoordinator
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.assembler.MusicPlayAssembler
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.volume.VolumeServer
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.setting.SkipTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.KoinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_download_list
import xymusic_kmp.composeapp.generated.resources.cancel_timer_close_message
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

@KoinViewModel
class MusicBottomMenuViewModel(
    private val settingsManager: SettingsManager,
    private val db: LocalDatabaseClient,
    private val downloadDb: DownloadDatabaseClient,
    val musicController: MusicCommonController,
    val dataSourceManager: DataSourceManager,
    val volumeServer: VolumeServer,
) : ViewModel() {

    val downloadMusicIdsFlow =
        db.downloadDao.getAllMusicTaskUidsFlow()
    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    var volumeValue by mutableStateOf(0f)
        private set

    private var volumeMaxValue: Int = 0
    private var volume: Int = 0

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

    init {
        getDoubleSpeed()
        initVolume()
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

    suspend fun setFavoriteMusic(
        itemId: String,
        ifFavorite: Boolean
    ): Boolean {
        val favoriteMusic = FavoriteCoordinator.setFavoriteData(
            dataSourceManager = dataSourceManager,
            type = MusicTypeEnum.MUSIC,
            itemId = itemId,
            ifFavorite = ifFavorite,
            musicController = musicController
        )
        return favoriteMusic
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
            delay(timerInfo * 60_000L)
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
            val downloadTypes =
                dataSourceManager.dataSourceType?.getDownloadType() ?: DownloadTypes.APK
            /*downloadManager.enqueue(
                DownloadRequest(
                    url = musicData.downloadUrl,
                    fileName = musicData.name + "." + musicData.container,
                    fileSize = musicData.size ?: 0,
                    uid = musicData.itemId,
                    title = musicData.name,
                    type = downloadTypes,
                    cover = musicData.pic,
                    duration = musicData.runTimeTicks,
                    mediaLibraryId = dataSourceManager.getConnectionId(),
                    music = musicData
                )
            )*/
            MessageUtils.sendPopTip(Res.string.add_download_list)
        }

    }

    fun addNextPlayer(itemId: String) {
        viewModelScope.launch {
            val playMusic = MusicPlayAssembler.attachFilePath(
                playMusic = db.musicDao.selectExtendById(itemId),
                downloadDb = downloadDb
            )
            playMusic?.let {
                musicController.addNextPlayer(it)
            }
        }

    }

    fun refreshVolume() {
        this.volumeValue = (volume.toFloat() / volumeMaxValue)
    }

    fun updateVolume(value: Float) {
        volumeServer.updateVolume((value * 100).toInt())
        this.volumeValue = value
    }

    fun initVolume() {
        volumeMaxValue = volumeServer.getMaxVolume()
        volume = volumeServer.getStreamVolume()
        refreshVolume()
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
        super.onCleared()
    }

    private fun cancelTimerJobs() {
        timerJob?.cancel()
        timerJob = null
        stopAfterCurrentTrackJob?.cancel()
        stopAfterCurrentTrackJob = null
    }

    private fun waitForCurrentTrackToFinish(currentItemId: String) {
        stopAfterCurrentTrackJob?.cancel()
        stopAfterCurrentTrackJob = viewModelScope.launch {
            var lastPosition = musicController.progressStateFlow.value

            musicController.progressStateFlow.collect { position ->
                val itemChanged = musicController.musicInfo?.itemId != currentItemId
                val playbackStopped =
                    musicController.state == PlayStateEnum.Pause || musicController.state == PlayStateEnum.None
                val progressRestarted = position < lastPosition

                if (itemChanged || playbackStopped || progressRestarted) {
                    stopPlaybackByTimer()
                    musicController.clearPlayerList()
                } else {
                    lastPosition = position
                }
            }
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

