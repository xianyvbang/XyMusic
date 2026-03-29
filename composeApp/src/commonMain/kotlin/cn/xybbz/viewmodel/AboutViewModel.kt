package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.info.getPlatformInfo
import cn.xybbz.di.ContextWrapper
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.download.XyDownload
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AboutViewModel(
    private val db: DatabaseClient,
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