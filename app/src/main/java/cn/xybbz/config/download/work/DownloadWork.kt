package cn.xybbz.config.download.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.exception.CancelDownloadException
import cn.xybbz.config.download.core.DownloadDispatcherImpl
import cn.xybbz.config.download.core.OkhttpDownloadCore
import cn.xybbz.config.download.notification.NotificationController
import cn.xybbz.config.download.state.DownloadState
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.enums.DownloadStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

class DownloadWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val db: DatabaseClient,
    private val callback: DownloadDispatcherImpl,
    private val dataSourceManager: IDataSourceManager,
    private val notificationController: NotificationController
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val downloadId = inputData.getLong(Constants.DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return Result.failure()
        val downloadTask = db.downloadDao.selectById(downloadId) ?: return Result.failure()
        val statusChange = suspend {
            db.downloadDao.getStatusById(downloadId)
        }
        val notificationId = downloadId.toInt() // 使用 taskId 作为通知 ID
        val okhttpDownloadCore = OkhttpDownloadCore(
            applicationContext
        )
        val initialNotification = notificationController.buildNotification(downloadTask, 0L).build()
        // Then, create the ForegroundInfo, specifying the type
        val foregroundInfo =
            notificationController.createForegroundInfo(notificationId, initialNotification)
        setForeground(foregroundInfo)
        val client = dataSourceManager.getApiClient(downloadTask.typeData)
        try {
            okhttpDownloadCore.download(client, statusChange, downloadTask).collect { state ->
                when (state) {
                    is DownloadState.InProgress -> {
                        callback.onProgress(
                            downloadId,
                            state.progress,
                            state.downloadedBytes,
                            state.totalBytes,
                            state.speedBps,
                            true
                        )
                    }

                    DownloadState.Success -> {
                        callback.onStateChanged(
                            downloadId,
                            DownloadStatus.COMPLETED,
                            downloadTask.filePath,
                            isSendNotice = true
                        )
                    }

                    DownloadState.Paused -> {
                        callback.onStateChanged(
                            downloadId,
                            DownloadStatus.PAUSED,
                            isSendNotice = true
                        )
                    }

                    is DownloadState.Error -> {
                        callback.onStateChanged(
                            downloadId,
                            DownloadStatus.FAILED,
                            error = state.message,
                            isSendNotice = true
                        )
                    }
                }

            }
        } catch (e: CancelDownloadException) {
            handleCancellation(downloadTask)
            return Result.success()
        } catch (e: Exception) {
            // 关键节点汇报：意外失败
            callback.onStateChanged(
                downloadId,
                DownloadStatus.FAILED,
                error = e.message ?: "未知错误",
                isSendNotice = true
            )
            return Result.failure()
        }
        return Result.success()
    }

    private suspend fun handleCancellation(task: XyDownload) {
        // 删除临时文件
        db.downloadDao.updateStatus(task.id, DownloadStatus.CANCEL)
        try {
            File(task.tempFilePath).delete()
        } catch (e: Exception) {
        }
    }
}