package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.info.getPlatformInfo
import cn.xybbz.platform.ContextWrapper
import cn.xybbz.localdata.config.LocalDatabaseClient
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AboutViewModel(
    private val db: LocalDatabaseClient,
    private val contextWrapper: ContextWrapper
) : ViewModel() {

    var versionInfo by mutableStateOf("")
        private set

    fun downloadAndInstall(
    ) {
        viewModelScope.launch {

        }
    }


    //取消下载
    fun cancelDownload() {

    }

    /**
     * 安装 APK
     */
    private fun installApk(
    ) {

    }

    override fun onCleared() {
        super.onCleared()
    }

    fun getPlatformInfo() {
        versionInfo = getPlatformInfo(contextWrapper).platformVersion
    }

}
