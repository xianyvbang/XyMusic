package cn.xybbz.viewmodel

import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.R
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.SettingsConfig
import cn.xybbz.config.alarm.AlarmConfig
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.download.core.DownloadRequest
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.setting.SkipTime
import cn.xybbz.localdata.enums.DownloadTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MusicBottomMenuViewModel @Inject constructor(
    private val settingsConfig: SettingsConfig,
    private val db: DatabaseClient,
    val musicController: MusicController,
    val dataSourceManager: IDataSourceManager,
    val alarmConfig: AlarmConfig,
    val connectionConfigServer: ConnectionConfigServer,
    val favoriteRepository: FavoriteRepository,
    val downloadManager: DownLoadManager
) : ViewModel() {



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
        settingsConfig.saveOrUpdateDoubleSpeed(value)
    }

    /**
     * 保存专辑跳过片头片尾
     */
    suspend fun saveOrUpdateSkipTimeData(skipTime: SkipTime) {
        skipTime.connectionId = connectionConfigServer.getConnectionId()
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
        val favoriteMusic = dataSourceManager.setFavoriteData(
            type = MusicTypeEnum.MUSIC,
            itemId = itemId,
            musicController = musicController,
            ifFavorite = ifFavorite
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
    fun createMusicStop() {
        alarmConfig.cancelAllAlarm()
        val calendar = android.icu.util.Calendar.getInstance()
            .apply { time = Date() } // 创建Calendar对象并设置为当前时间
        calendar.add(Calendar.MINUTE, timerInfo.toInt())
        alarmConfig.getUpAlarmManagerStartWork(calendar, ifPlayEndClose)
        Log.i("=====", "触发时间${SimpleDateFormat.getTimeInstance().format(calendar.time)}")
        MessageUtils.sendPopTip(
            SimpleDateFormat.getTimeInstance().format(calendar.time), R.string.timer_close_message
        )

    }

    /**
     * 取消音乐app定时关闭
     */
    fun cancelAlarm() {
        viewModelScope.launch {
            alarmConfig.cancelAllAlarm()
            MessageUtils.sendPopTip(
                R.string.cancel_timer_close_message
            )
        }
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
    fun getArtistInfos(artists: String) {
        viewModelScope.launch {
            try {
                xyArtists =
                    dataSourceManager.selectArtistInfoByIds(artists.split(Constants.ARTIST_DELIMITER))
                        ?: emptyList()
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "获得艺术家信息失败", e)
            }

        }
    }

    suspend fun downloadMusic(musicData: XyMusic) {
        val downloadTypes = dataSourceManager.dataSourceType?.getDownloadType() ?: DownloadTypes.APK
        downloadManager.enqueue(
            DownloadRequest(
                url = musicData.downloadUrl,
                fileName = musicData.name + "." + musicData.container,
                fileSize = musicData.size ?: 0,
                uid = musicData.itemId,
                title = musicData.name,
                type = downloadTypes,
                cover = musicData.pic,
                duration = musicData.runTimeTicks,
                connectionId = connectionConfigServer.getConnectionId(),
                music = musicData
            )
        )
    }

    fun addNextPlayer(itemId: String){
        viewModelScope.launch {
            val playMusic = db.musicDao.selectExtendById(itemId)
            playMusic?.let {
                musicController.addNextPlayer(it)
            }
        }

    }

}