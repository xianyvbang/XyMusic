package cn.xybbz.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.download.core.DownloadListener
import cn.xybbz.config.download.core.DownloadRequest
import cn.xybbz.config.update.ApkUpdateManager
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.enums.DownloadStatus
import cn.xybbz.localdata.enums.DownloadTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    val backgroundConfig: BackgroundConfig,
    private val db: DatabaseClient,
    private val downloadManager: DownLoadManager,
    val apkUpdateManager: ApkUpdateManager

) : ViewModel() {


    private val downloadListener = object : DownloadListener {
        override fun onTaskUpdated(task: XyDownload) {
            // 在这里，你可以收到每一个任务的实时更新
            // 例如，可以打印日志来观察
            Log.d("DownloadListener", "Task Updated: task:$task")

            // 对于更复杂的场景，你可以在这里更新一个单独的 StateFlow
            // 来驱动 UI 上某个特定元素的实时刷新
            if (task.status == DownloadStatus.COMPLETED) {
                installApk(downloadManager.applicationContext, File(task.filePath, task.fileName))
            }
        }
    }

    init {
        downloadManager.addListener(downloadListener)
    }

    val apkDownloadInfo: StateFlow<XyDownload?> = db.apkDownloadDao.getOneApkFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    suspend fun downloadAndInstall(
        apkUrl: String,
        apkName: String,
        apkSize: Long
    ) {
        //判断是否下载中,如果有下载中则不能继续下载
        val apkDownload = db.apkDownloadDao.getByTypeAndUrl(DownloadTypes.APK, apkUrl)
        if (apkDownload != null && apkDownload.status != DownloadStatus.COMPLETED) {
            downloadManager.resume(apkDownload.id)
        } else if (apkDownload != null && apkDownload.status == DownloadStatus.COMPLETED) {
            installApk(
                downloadManager.applicationContext,
                File(apkDownload.filePath, apkDownload.fileName)
            )
        } else {
            downloadManager.enqueue(
                DownloadRequest(
                    url = apkUrl,
                    fileName = apkName,
                    fileSize = apkSize
                )
            )
        }
    }


    //取消下载
    fun cancelDownload() {
        apkDownloadInfo.value?.id?.let { id ->
            downloadManager.cancel(id)
        }
    }

    /**
     * 安装 APK
     */
    private fun installApk(
        context: Context,
        apkFile: File,
    ) {
        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(intent)
    }

    override fun onCleared() {
        super.onCleared()
        downloadManager.removerListener(downloadListener)
    }

}