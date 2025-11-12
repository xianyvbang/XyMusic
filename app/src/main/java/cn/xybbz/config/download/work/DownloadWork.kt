package cn.xybbz.config.download.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cn.xybbz.api.client.version.VersionApiClient
import cn.xybbz.common.constants.Constants
import cn.xybbz.config.download.core.DownloadDispatcherImpl
import cn.xybbz.config.download.core.OkhttpDownloadCore
import cn.xybbz.download.state.DownloadState
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.enums.DownloadStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class DownloadWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val db: DatabaseClient,
    private val callback: DownloadDispatcherImpl,
    private val client: VersionApiClient,
) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val downloadId = inputData.getLong(Constants.DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return Result.failure()
        val downloadTask = db.downloadDao.selectById(downloadId) ?: return Result.failure()
        val statusChange = suspend { db.downloadDao.getStatusById(downloadId) }

        val okhttpDownloadCore = OkhttpDownloadCore(
            applicationContext
        )
        try {
            okhttpDownloadCore.download(client, statusChange, downloadTask).collect { state ->
                when (state) {
                    is DownloadState.InProgress -> {
                        callback.onProgress(
                            downloadId,
                            state.progress,
                            state.downloadedBytes,
                            state.totalBytes,
                            state.speedBps
                        )
                    }

                    DownloadState.Success -> {
                        callback.onStateChanged(
                            downloadId,
                            DownloadStatus.COMPLETED,
                            downloadTask.filePath
                        )
                    }

                    DownloadState.Paused -> {
                        callback.onStateChanged(downloadId, DownloadStatus.PAUSED)
                    }

                    is DownloadState.Error -> {
                        callback.onStateChanged(
                            downloadId,
                            DownloadStatus.FAILED,
                            error = state.message
                        )
                    }
                }

            }
        } catch (e: CancellationException) {
            handleCancellation(downloadTask)
            return Result.success()
        } catch (e: Exception) {
            // 关键节点汇报：意外失败
            callback.onStateChanged(
                downloadId,
                DownloadStatus.FAILED,
                error = e.message ?: "未知错误"
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