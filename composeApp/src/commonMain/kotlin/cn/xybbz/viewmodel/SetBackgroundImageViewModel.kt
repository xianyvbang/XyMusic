package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.setting.SettingsManager
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SetBackgroundImageViewModel (
    val settingsManager: SettingsManager,
): ViewModel() {

    fun updateBackgroundImagePath(imageFilePath: String?) {
        viewModelScope.launch {
            settingsManager.setImageFilePath(imageFilePath)
        }
    }
}
