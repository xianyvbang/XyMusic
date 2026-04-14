package cn.xybbz.download.core

import cn.xybbz.config.download.state.DownloadState
import cn.xybbz.download.DownloadGlobal
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.internal.DownloadCancellationException
import cn.xybbz.download.internal.DownloadLogger
import cn.xybbz.platform.ContextWrapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

actual class DownloadTaskScheduler actual constructor(
    private val contextWrapper: ContextWrapper,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = mutableMapOf<Long, Job>()
    private val jobsLock = Any()

    actual fun enqueue(downloadId: Long) {
        val job = scope.launch(
            context = CoroutineName("xy-download-$downloadId"),
            start = CoroutineStart.LAZY,
        ) {
            val currentJob = currentCoroutineContext()[Job]
            try {
                executeDownload(downloadId)
            } finally {
                synchronized(jobsLock) {
                    if (currentJob != null && jobs[downloadId] === currentJob) {
                        jobs.remove(downloadId)
                    }
                }
            }
        }

        val shouldStart = synchronized(jobsLock) {
            val existing = jobs[downloadId]
            if (existing != null && existing.isActive) {
                false
            } else {
                jobs[downloadId] = job
                true
            }
        }

        if (shouldStart) {
            job.start()
        } else {
            job.cancel()
        }
    }

    actual fun cancel(downloadId: Long) {
        val job = synchronized(jobsLock) {
            jobs.remove(downloadId)
        }
        job?.cancel()
    }

    private suspend fun executeDownload(downloadId: Long) {
        val downloadGlobal = DownloadGlobal.downloaderManagerGlobal
        val downloadTask = downloadGlobal.db.downloadDao.selectById(downloadId) ?: return
        val statusChange = suspend {
            downloadGlobal.db.downloadDao.getStatusById(downloadId)
        }
        val httpClient = downloadGlobal.httpClientFactory.createHttpClient()

        try {
            HttpClientDownloadCore().download(
                client = httpClient,
                contextWrapper = contextWrapper,
                statusChange = statusChange,
                xyDownload = downloadTask,
            ).collect { state ->
                when (state) {
                    is DownloadState.InProgress -> {
                        downloadGlobal.downloadDispatcher.onProgress(
                            downloadId = downloadId,
                            progress = state.progress,
                            downloadedBytes = state.downloadedBytes,
                            totalBytes = state.totalBytes,
                            speedBps = state.speedBps,
                            isSendNotice = true,
                        )
                    }

                    DownloadState.Success -> {
                        downloadGlobal.downloadDispatcher.onStateChanged(
                            downloadId = downloadId,
                            status = DownloadStatus.COMPLETED,
                            finalPath = downloadTask.filePath,
                            isSendNotice = true,
                        )
                    }

                    DownloadState.Paused -> {
                        downloadGlobal.downloadDispatcher.onStateChanged(
                            downloadId = downloadId,
                            status = DownloadStatus.PAUSED,
                            isSendNotice = true,
                        )
                    }

                    is DownloadState.Error -> {
                        downloadGlobal.downloadDispatcher.onStateChanged(
                            downloadId = downloadId,
                            status = DownloadStatus.FAILED,
                            error = state.message,
                            isSendNotice = true,
                        )
                    }
                }
            }
        } catch (_: DownloadCancellationException) {
            DownloadLogger.d(DownloadConstants.LOG_TAG, "Download $downloadId cancelled.")
        } catch (_: CancellationException) {
            DownloadLogger.d(DownloadConstants.LOG_TAG, "Download job $downloadId cancelled.")
        } catch (e: Exception) {
            downloadGlobal.downloadDispatcher.onStateChanged(
                downloadId = downloadId,
                status = DownloadStatus.FAILED,
                error = e.message ?: "unknown error",
                isSendNotice = true,
            )
        } finally {
            httpClient.close()
        }
    }
}
