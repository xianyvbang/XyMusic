package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.SettingsConfig
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.setting.XySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val _settingsConfig: SettingsConfig,
    private val db: DatabaseClient,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    val settingsConfig = _settingsConfig
    val backgroundConfig = _backgroundConfig

    /**
     * 设置信息
     */
    var settingDataNow by mutableStateOf(
        XySettings()
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
                    settingsConfig.setSettingsData(it)
                }

            }
        }
    }



}