package cn.xybbz.viewmodel

import android.net.Uri
import androidx.compose.ui.graphics.Color
import cn.xybbz.config.setting.SettingsManager
import org.koin.core.annotation.Singleton

@Singleton
class BackgroundConfig(
    private val settingsManager: SettingsManager
) {

    val imageFilePath: String?
        get() = settingsManager.imageFilePath

    val settingsBrash: List<Color>
        get() = listOf(Color(0xFF503803), Color(0xFF04727E))

    val cacheLimitBrash: List<Color>
        get() = listOf(Color(0xFF503803), Color(0xFF04727E))

    suspend fun updateBackgroundImageUri(backgroundImageUri: Uri?) {
        settingsManager.setImageFilePath(backgroundImageUri?.toString())
    }
}
