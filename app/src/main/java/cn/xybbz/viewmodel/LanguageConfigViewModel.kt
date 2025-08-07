package cn.xybbz.viewmodel

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.SettingsConfig
import cn.xybbz.localdata.enums.LanguageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LanguageConfigViewModel @Inject constructor(
    private val _settingsConfig: SettingsConfig
) : ViewModel() {

    val settingsConfig = _settingsConfig
    var applicationLanguage by mutableStateOf("")
        private set

    fun getApplicationLanguageData(context: Context){
        val currentAppLocales: LocaleList =
            context.getSystemService(LocaleManager::class.java).applicationLocales
        applicationLanguage = currentAppLocales.toLanguageTags()
    }
    fun updateLanguageType(languageType: LanguageType, context: Context) {
        viewModelScope.launch {
            _settingsConfig.setLanguageTypeData(languageType, context)
        }
    }
}