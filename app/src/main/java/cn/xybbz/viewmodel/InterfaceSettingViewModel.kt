package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.setting.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InterfaceSettingViewModel @Inject constructor(
    val backgroundConfig: BackgroundConfig,
    val settingsManager: SettingsManager
) : ViewModel() {

}