package cn.xybbz.viewmodel

import android.provider.Contacts.Settings.getSetting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.SettingsConfig
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.module.SettingsModule_SettingsConfigFactory.settingsConfig
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.setting.XySettings
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
}