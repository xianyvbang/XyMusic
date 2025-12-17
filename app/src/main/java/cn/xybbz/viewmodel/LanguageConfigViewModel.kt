package cn.xybbz.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.LanguageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LanguageConfigViewModel @Inject constructor(
    private val _settingsManager: SettingsManager,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    val backgroundConfig = _backgroundConfig

    val settingsConfig = _settingsManager

    fun updateLanguageType(languageType: LanguageType, context: Context) {
        viewModelScope.launch {
            _settingsManager.setLanguageTypeData(languageType, context)
        }
    }
}