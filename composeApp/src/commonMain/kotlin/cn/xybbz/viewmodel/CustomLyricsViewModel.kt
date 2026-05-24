package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.setting.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.KoinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.save_failed
import xymusic_kmp.composeapp.generated.resources.save_success

@KoinViewModel
class CustomLyricsViewModel (
    private val settingsManager: SettingsManager
) : ViewModel() {

    //自定义API文档地址
    val url = "https://docs.lrc.cx/docs/QuickStart"

    /**
     * 是否优先走音乐服务自身资源接口（歌词/封面）
     */
    var ifPriorityMusicApi by mutableStateOf(settingsManager.get().ifPriorityMusicApi)
        private set

    /**
     * 单曲歌词接口地址输入值（/lyrics）
     */
    var customLrcSingleApiValue by mutableStateOf(
        settingsManager.get().customLrcSingleApi
    )
        private set

    /**
     * 自定义歌词接口鉴权 key 输入值
     */
    var customLrcApiAuthValue by mutableStateOf(
        settingsManager.get().customLrcApiAuth
    )
        private set

    /**
     * 自定义封面接口地址输入值（/cover）
     */
    var customCoverApiValue by mutableStateOf(
        settingsManager.get().customCoverApi
    )
        private set

    fun updateIfPriorityMusicApi(ifPriorityMusicApi: Boolean) {
        this.ifPriorityMusicApi = ifPriorityMusicApi
    }

    fun updateCustomLrcSingleApi(customLrcSingleApi: String) {
        customLrcSingleApiValue = customLrcSingleApi
    }

    fun updateCustomLrcApiAuth(customLrcApiAuth: String) {
        customLrcApiAuthValue = customLrcApiAuth
    }

    fun updateCustomCoverApi(customCoverApi: String) {
        customCoverApiValue =customCoverApi
    }

    /**
     * 保存页面配置到本地设置表
     */
    fun saveSettings() {
        viewModelScope.launch {
            try {

                withContext(Dispatchers.IO) {
                    settingsManager.setIfPriorityMusicApi(ifPriorityMusicApi)
                    settingsManager.setCustomLrcSingleApi(customLrcSingleApiValue.trim())
                    settingsManager.setCustomLrcApiAuth(customLrcApiAuthValue.trim())
                    settingsManager.setCustomCoverApi(customCoverApiValue.trim())
                }
                MessageUtils.sendPopTipSuccess(Res.string.save_success)
            } catch (e: Exception) {
                e.printStackTrace()
                MessageUtils.sendPopTipError(Res.string.save_failed)
            }
        }

    }
}
