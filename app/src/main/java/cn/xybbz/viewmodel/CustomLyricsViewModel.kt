package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.setting.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CustomLyricsViewModel @Inject constructor(
    val backgroundConfig: BackgroundConfig,
    private val settingsManager: SettingsManager
) : ViewModel() {

    /**
     * 是否优先走音乐服务自身歌词接口
     */
    var ifPriorityMusicApi by mutableStateOf(settingsManager.get().ifPriorityMusicApi)
        private set

    /**
     * 单曲歌词接口地址输入值（/lyrics）
     */
    var customLrcSingleApiValue by mutableStateOf(
        TextFieldValue(
            text = settingsManager.get().customLrcSingleApi,
            selection = TextRange(settingsManager.get().customLrcSingleApi.length)
        )
    )
        private set

    /**
     * 自定义歌词接口鉴权 key 输入值
     */
    var customLrcApiAuthValue by mutableStateOf(
        TextFieldValue(
            text = settingsManager.get().customLrcApiAuth,
            selection = TextRange(settingsManager.get().customLrcApiAuth.length)
        )
    )
        private set

    /**
     * 自定义封面接口地址输入值（/cover）
     */
    var customCoverApiValue by mutableStateOf(
        TextFieldValue(
            text = settingsManager.get().customCoverApi,
            selection = TextRange(settingsManager.get().customCoverApi.length)
        )
    )
        private set

    fun updateIfPriorityMusicApi(ifPriorityMusicApi: Boolean) {
        this.ifPriorityMusicApi = ifPriorityMusicApi
    }

    fun updateCustomLrcSingleApi(customLrcSingleApi: String) {
        customLrcSingleApiValue = TextFieldValue(
            text = customLrcSingleApi,
            selection = TextRange(customLrcSingleApi.length)
        )
    }

    fun updateCustomLrcApiAuth(customLrcApiAuth: String) {
        customLrcApiAuthValue = TextFieldValue(
            text = customLrcApiAuth,
            selection = TextRange(customLrcApiAuth.length)
        )
    }

    fun updateCustomCoverApi(customCoverApi: String) {
        customCoverApiValue = TextFieldValue(
            text = customCoverApi,
            selection = TextRange(customCoverApi.length)
        )
    }

    /**
     * 保存页面配置到本地设置表
     */
    suspend fun saveSettings() {
        settingsManager.setIfPriorityMusicApi(ifPriorityMusicApi)
        settingsManager.setCustomLrcSingleApi(customLrcSingleApiValue.text.trim())
        settingsManager.setCustomLrcApiAuth(customLrcApiAuthValue.text.trim())
        settingsManager.setCustomCoverApi(customCoverApiValue.text.trim())
    }
}
