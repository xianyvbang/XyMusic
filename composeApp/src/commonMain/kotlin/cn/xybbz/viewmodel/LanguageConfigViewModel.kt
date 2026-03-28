package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.LanguageType
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel


@KoinViewModel
class LanguageConfigViewModel(
    val settingsManager: SettingsManager,
) : ViewModel() {


    fun updateLanguageType(languageType: LanguageType) {
        viewModelScope.launch {
            settingsManager.setLanguageTypeData(languageType)
        }
    }
}
