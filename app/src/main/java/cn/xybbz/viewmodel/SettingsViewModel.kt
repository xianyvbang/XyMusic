package cn.xybbz.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.SettingsConfig
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.download.core.DownloaderConfig
import cn.xybbz.localdata.config.DatabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsConfig: SettingsConfig,
    private val db: DatabaseClient,
    private val _backgroundConfig: BackgroundConfig,
    val downLoadManager: DownLoadManager
) : ViewModel() {

    val backgroundConfig = _backgroundConfig

    /**
     * 设置信息
     */
    var settingDataNow by mutableStateOf(
        settingsConfig.get()
    )

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

    suspend fun setMaxConcurrentDownloads(maxConcurrentDownloads: Int, context: Context) {
        settingsConfig.setMaxConcurrentDownloads(maxConcurrentDownloads)
        val downloaderConfig = downLoadManager.getConfig()
        downLoadManager.updateConfig(
            DownloaderConfig.Builder(context).setMaxConcurrentDownloads(maxConcurrentDownloads)
                .setIfOnlyWifiDownload(downloaderConfig.ifOnlyWifiDownload)
                .build()
        )
    }

    suspend fun setIfOnlyWifiDownload(ifOnlyWifiDownload: Boolean, context: Context) {
        settingsConfig.setIfOnlyWifiDownload(ifOnlyWifiDownload)
        val downloaderConfig = downLoadManager.getConfig()
        downLoadManager.updateConfig(
            DownloaderConfig.Builder(context)
                .setMaxConcurrentDownloads(downloaderConfig.maxConcurrentDownloads)
                .setIfOnlyWifiDownload(ifOnlyWifiDownload)
                .build()
        )
    }
}