package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.PlaybackProgressReporter
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.download.DownloaderManager
import cn.xybbz.localdata.config.LocalDatabaseClient
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel(
    val settingsManager: SettingsManager,
    private val db: LocalDatabaseClient,
    private val musicController: MusicCommonController,
    private val playbackProgressReporter: PlaybackProgressReporter,
    private val dataSourceManager: DataSourceManager,
    private val downloaderManager: DownloaderManager
) : ViewModel() {


    /**
     * 设置信息
     */
    var settingDataNow by mutableStateOf(
        settingsManager.get()
    )

    val songStoragePath: String
        get() = downloaderManager.config.finalDirectory

    init {
        getSetting()
    }

    /**
     * 获得系统设置
     */
    private fun getSetting() {
        viewModelScope.launch {
            db.settingsDao.selectOne().distinctUntilChanged().collect {
                if (it != null) {
                    settingDataNow = it
                }
            }
        }
    }

    suspend fun setMaxConcurrentDownloads(maxConcurrentDownloads: Int) {
        settingsManager.setMaxConcurrentDownloads(maxConcurrentDownloads)
        /* downLoadManager.updateConfig(
             DownloaderConfig.Builder(context).setMaxConcurrentDownloads(maxConcurrentDownloads)
                 .build()
         )*/
    }

    suspend fun setSyncPlayProgressEnabled(enabled: Boolean) {
        val wasEnabled = settingsManager.get().ifEnableSyncPlayProgress
        if (wasEnabled == enabled) {
            return
        }
        if (!enabled) {
            playbackProgressReporter.stop()
            val currentMusicId = musicController.musicInfo?.itemId
            if (musicController.state == PlayStateEnum.Playing && !currentMusicId.isNullOrBlank()) {
                runCatching {
                    dataSourceManager.cancelReportProgress(currentMusicId)
                }.onFailure {
                    Log.e(Constants.LOG_ERROR_PREFIX, "关闭播放进度同步时通知远端停止失败", it)
                }
            }
        }
        settingsManager.setIfEnableSyncPlayProgress(enabled)
    }
}
