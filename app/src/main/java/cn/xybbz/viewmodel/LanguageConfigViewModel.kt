package cn.xybbz.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.SettingsConfig
import cn.xybbz.localdata.enums.LanguageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LanguageConfigViewModel @Inject constructor(
    private val _settingsConfig: SettingsConfig,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    val backgroundConfig = _backgroundConfig

    val settingsConfig = _settingsConfig

    fun updateLanguageType(languageType: LanguageType, context: Context) {
        viewModelScope.launch {
            _settingsConfig.setLanguageTypeData(languageType, context)
        }
    }
}