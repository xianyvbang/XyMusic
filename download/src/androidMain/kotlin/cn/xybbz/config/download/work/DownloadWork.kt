package cn.xybbz.config.download.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cn.xybbz.config.download.core.HttpClientDownloadCore
import cn.xybbz.config.download.notification.NotificationController
import cn.xybbz.config.download.state.DownloadState
import cn.xybbz.download.core.DownloadConstants
import cn.xybbz.download.core.DownloadDispatcherImpl
import cn.xybbz.download.core.DownloadPlatformFiles
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.internal.DownloadCancellationException
import cn.xybbz.platform.ContextWrapper
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.collect

// Android 后台 worker 保持原职责：拉起下载核心、转发进度、同步最终状态。
class DownloadWork(
    appContext: Context,
    workerParams: WorkerParameters,
    private val db: DownloadDatabaseClient,
    private val callback: DownloadDispatcherImpl,
    private val notificationController: NotificationController,
    private val httpClient: HttpClient
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val downloadId = inputData.getLong(DownloadConstants.WORK_INPUT_DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return Result.failure()
        val downloadTask = db.downloadDao.selectById(downloadId) ?: return Result.failure()
        val statusChange = suspend {
            db.downloadDao.getStatusById(downloadId)
        }
        val notificationId = downloadId.toInt()
        val httpClientDownloadCore = HttpClientDownloadCore()
        val initialNotification = notificationController
            .buildNotification(downloadTask, 0L)
            .build()
        setForeground(
            notificationController.createForegroundInfo(notificationId, initialNotification)
        )
        try {
            httpClientDownloadCore.download(
                client = httpClient,
                contextWrapper = ContextWrapper(applicationContext),
                statusChange = statusChange,
                xyDownload = downloadTask,
            )
                .collect { state ->
                    when (state) {
                        is DownloadState.InProgress -> {
                            callback.onProgress(
                                downloadId,
                                state.progress,
                                state.downloadedBytes,
                                state.totalBytes,
                                state.speedBps,
                                true,
                            )
                        }

                        DownloadState.Success -> {
                            callback.onStateChanged(
                                downloadId,
                                DownloadStatus.COMPLETED,
                                downloadTask.filePath,
                                isSendNotice = true,
                            )
                        }

                        DownloadState.Paused -> {
                            callback.onStateChanged(
                                downloadId,
                                DownloadStatus.PAUSED,
                                isSendNotice = true,
                            )
                        }

                        is DownloadState.Error -> {
                            callback.onStateChanged(
                                downloadId,
                                DownloadStatus.FAILED,
                                error = state.message,
                                isSendNotice = true,
                            )
                        }
                    }
                }
        } catch (e: DownloadCancellationException) {
            handleCancellation(downloadTask.id, downloadTask.tempFilePath)
            return Result.success()
        } catch (e: Exception) {
            callback.onStateChanged(
                downloadId,
                DownloadStatus.FAILED,
                error = e.message ?: "unknown error",
                isSendNotice = true,
            )
            return Result.failure()
        }
        return Result.success()
    }

    private suspend fun handleCancellation(taskId: Long, tempFilePath: String) {
        db.downloadDao.updateStatus(taskId, DownloadStatus.CANCEL)
        DownloadPlatformFiles.deleteFile(tempFilePath)
    }
}
