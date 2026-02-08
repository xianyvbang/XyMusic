package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.ThemeTypeEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterfaceSettingViewModel @Inject constructor(
    val backgroundConfig: BackgroundConfig,
    val settingsManager: SettingsManager
) : ViewModel() {


    fun setThemeTypeData(themeType: ThemeTypeEnum){
        viewModelScope.launch {
            settingsManager.setThemeTypeData(themeType)
            backgroundConfig.updateIfEnabled(themeType == ThemeTypeEnum.FLOWER)
        }

    }
}