package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import cn.xybbz.config.download.DownLoadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LocalViewModel @Inject constructor(
    private val downLoadManager: DownLoadManager
) : ViewModel() {

    fun getAppDownloadFiles(): List<File> {
        val downloadDir = File(downLoadManager.downloadDispatcher.config.finalDirectory)

        if (!downloadDir.exists()) {
            return emptyList()
        }

        return downloadDir.listFiles()?.toList() ?: emptyList()
    }

}