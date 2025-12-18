package cn.xybbz.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.download.core.DownloaderConfig
import cn.xybbz.localdata.config.DatabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsManager: SettingsManager,
    private val db: DatabaseClient,
    val backgroundConfig: BackgroundConfig,
    val downLoadManager: DownLoadManager
) : ViewModel() {


    /**
     * 设置信息
     */
    var settingDataNow by mutableStateOf(
        settingsManager.get()
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
        settingsManager.setMaxConcurrentDownloads(maxConcurrentDownloads)
        downLoadManager.updateConfig(
            DownloaderConfig.Builder(context).setMaxConcurrentDownloads(maxConcurrentDownloads)
                .build()
        )
    }
}