package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import cn.xybbz.config.SettingsConfig
import cn.xybbz.localdata.enums.ThemeTypeEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InterfaceSettingViewModel @Inject constructor(
    private val _settingsConfig: SettingsConfig
) : ViewModel() {

    val settingsConfig = _settingsConfig

    suspend fun updateTheme(themeType: ThemeTypeEnum) {
        _settingsConfig.setThemeTypeData(themeType)
    }

    suspend fun updateDynamicColor(dynamicColor: Boolean) {
        _settingsConfig.setIsDynamic(dynamicColor)
    }
}