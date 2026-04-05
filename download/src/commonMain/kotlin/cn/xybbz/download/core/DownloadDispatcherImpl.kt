/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.download.core

import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.internal.DownloadIoScoped
import cn.xybbz.download.internal.DownloadLogger
import cn.xybbz.download.notification.DownloadNotificationDelegate
import cn.xybbz.platform.ContextWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class DownloadDispatcherImpl(
    private val contextWrapper: ContextWrapper,
    private val db: DownloadDatabaseClient,
    private val taskScheduler: DownloadTaskScheduler = DownloadTaskScheduler(contextWrapper),
    var config: DownloaderConfig,
    val notificationDelegate: DownloadNotificationDelegate = DownloadNotificationDelegate(
        contextWrapper
    ),
) : IDownloadDispatcher, DownloadIoScoped() {

    // 任务状态切换统一收敛到这把锁里，保证内存态和数据库态的切换顺序一致。
    private val globalMutex = Mutex()

    private val readyTasks = ArrayDeque<XyDownload>()
    private val runningTasks = mutableMapOf<Long, XyDownload>()
    private val pausedTasks = mutableMapOf<Long, XyDownload>()
    private val failedTasks = mutableMapOf<Long, XyDownload>()

    private val _taskUpdateEventFlow = MutableSharedFlow<XyDownload>(extraBufferCapacity = 64)
    val taskUpdateEventFlow = _taskUpdateEventFlow.asSharedFlow()
    private var lastDbUpdateTime = 0L
    private val dbUpdateInterval = 1000L

    init {
        createScope()
    }

    suspend fun rehydrate(mediaLibraryId: String?) =
        withContext(Dispatchers.IO) {
            readyTasks.clear()
            runningTasks.clear()
            pausedTasks.clear()
            failedTasks.clear()

            db.downloadDao.getAllTasksSuspend(mediaLibraryId).forEach { task ->
                when (task.status) {
                    DownloadStatus.QUEUED -> readyTasks.add(task)
                    DownloadStatus.DOWNLOADING -> {
                        task.status = DownloadStatus.QUEUED
                        readyTasks.add(task)
                    }

                    DownloadStatus.PAUSED -> pausedTasks[task.id] = task
                    DownloadStatus.FAILED -> failedTasks[task.id] = task
                    else -> Unit
                }
            }
            promoteAndExecute()
        }

    fun enqueue(newTasks: List<XyDownload>) {
        scope.launch {
            // 新任务先进入 ready 队列，再尝试补位执行。
            readyTasks.addAll(newTasks)
            promoteAndExecute()
        }
    }

    fun updateConfig(newConfig: DownloaderConfig) {
        config = newConfig
        promoteAndExecute()
    }

    fun pause(ids: List<Long>) {
        scope.launch {
            ids.forEach { id ->
                var task: XyDownload? = null
                readyTasks.firstOrNull { it.id == id }?.let {
                    task = it
                    readyTasks.remove(it)
                }
                runningTasks[id]?.let {
                    task = it
                }
                task?.let {
                    it.status = DownloadStatus.PAUSED
                    pausedTasks[it.id] = it
                    db.downloadDao.updateStatus(it.id, DownloadStatus.PAUSED)
                }
            }
            promoteAndExecute()
        }
    }

    fun resume(ids: List<Long>) {
        scope.launch {
            ids.forEach { id ->
                pausedTasks.remove(id)?.let {
                    it.status = DownloadStatus.QUEUED
                    readyTasks.add(it)
                    db.downloadDao.updateStatus(it.id, DownloadStatus.QUEUED)
                }
                failedTasks.remove(id)?.let {
                    it.status = DownloadStatus.QUEUED
                    readyTasks.add(it)
                    db.downloadDao.updateStatus(it.id, DownloadStatus.QUEUED)
                }
            }
            promoteAndExecute()
        }
    }

    fun cancel(ids: List<Long>) {
        scope.launch {
            val time = Clock.System.now().toEpochMilliseconds()
            val tasksToCancel = mutableListOf<XyDownload>()
            ids.forEach { id ->
                readyTasks.firstOrNull { it.id == id }
                    ?.also { readyTasks.remove(it); tasksToCancel.add(it) }
                runningTasks.remove(id)?.also { tasksToCancel.add(it) }
                pausedTasks.remove(id)?.also { tasksToCancel.add(it) }
                failedTasks.remove(id)?.also { tasksToCancel.add(it) }
            }

            if (tasksToCancel.isNotEmpty()) {
                val taskIds = tasksToCancel.map { it.id }
                db.downloadDao.updateStatuses(taskIds, DownloadStatus.CANCEL, time)

                tasksToCancel.forEach { task ->
                    taskScheduler.cancel(task.id)
                    try {
                        DownloadPlatformFiles.deleteFile(task.tempFilePath)
                        cleanupTaskFiles(task)
                    } catch (e: Exception) {
                        DownloadLogger.d(DownloadConstants.LOG_TAG, e.toString())
                    }
                }
            }
            promoteAndExecute()
        }
    }

    fun cancelAll() {
        cancel(runningTasks.values.map { it.id } + readyTasks.map { it.id })
    }

    fun pauseAll() {
        pause(runningTasks.values.map { it.id } + readyTasks.map { it.id })
    }

    fun delete(ids: List<Long>, deleteFile: Boolean) {
        scope.launch {
            val tasksToDelete = mutableListOf<XyDownload>()
            ids.forEach { id ->
                readyTasks.firstOrNull { it.id == id }
                    ?.also { readyTasks.remove(it); tasksToDelete.add(it) }
                runningTasks.remove(id)?.also { tasksToDelete.add(it) }
                pausedTasks.remove(id)?.also { tasksToDelete.add(it) }
                failedTasks.remove(id)?.also { tasksToDelete.add(it) }
            }

            if (tasksToDelete.isNotEmpty()) {
                tasksToDelete.forEach { task ->
                    taskScheduler.cancel(task.id)
                    try {
                        cleanupTaskFiles(task)
                        if (deleteFile) {
                            DownloadPlatformFiles.deleteFile(task.filePath)
                        }
                    } catch (e: Exception) {
                        DownloadLogger.d(DownloadConstants.LOG_TAG, e.toString())
                    }
                }
            }
            db.downloadDao.deleteById(*ids.toLongArray())
            promoteAndExecute()
        }
    }

    private fun promoteAndExecute() = scope.launch {
        globalMutex.withLock {
            // 只要并发槽位有空闲，就持续从 ready 队列提升任务。
            while (runningTasks.size < config.maxConcurrentDownloads) {
                val task = readyTasks.removeFirstOrNull() ?: break
                task.status = DownloadStatus.DOWNLOADING
                runningTasks[task.id] = task
                startDownloadWorker(task)
            }
        }
    }

    private suspend fun startDownloadWorker(task: XyDownload) {
        // 真正的后台执行细节交给平台调度器，这里只负责切状态和触发。
        db.downloadDao.updateStatus(task.id, DownloadStatus.DOWNLOADING)
        taskScheduler.enqueue(task.id)
    }

    override fun onProgress(
        downloadId: Long,
        progress: Float,
        downloadedBytes: Long,
        totalBytes: Long,
        speedBps: Long,
        isSendNotice: Boolean,
    ) {
        val download = runningTasks[downloadId]
        download?.let {
            it.progress = progress
            it.downloadedBytes = downloadedBytes
            it.totalBytes = totalBytes
        }
        val currentTime = Clock.System.now().toEpochMilliseconds()

        if (download != null) {
            scope.launch {
                if (isSendNotice) {
                    notificationDelegate.showProgress(download, speedBps)
                }
                // UI 层只关心最新快照，这里把更新后的任务重新广播出去。
                _taskUpdateEventFlow.emit(download.copy(updateTime = currentTime))
            }
        }

        scope.launch {
            if (currentTime - lastDbUpdateTime >= dbUpdateInterval) {
                lastDbUpdateTime = currentTime
                scope.launch(Dispatchers.IO) {
                    runningTasks.values.forEach { runningTask ->
                        db.downloadDao.updateProgress(
                            runningTask.id,
                            runningTask.progress,
                            runningTask.downloadedBytes,
                            runningTask.totalBytes,
                            currentTime,
                        )
                    }
                }
            }
        }
    }

    override fun onStateChanged(
        downloadId: Long,
        status: DownloadStatus,
        finalPath: String?,
        error: String?,
        isSendNotice: Boolean,
    ) {
        scope.launch {
            var updateTask: XyDownload? = null

            globalMutex.withLock {
                val time = Clock.System.now().toEpochMilliseconds()
                val task = runningTasks.remove(downloadId)
                if (task != null) {
                    updateTask = task
                    task.status = status
                    _taskUpdateEventFlow.emit(task.copy(updateTime = time))
                    when (status) {
                        DownloadStatus.COMPLETED -> {
                            if (finalPath != null) {
                                db.downloadDao.updateOnSuccess(downloadId, status, finalPath, time)
                            }
                        }

                        DownloadStatus.PAUSED -> {
                            pausedTasks[task.id] = task
                            db.downloadDao.updateStatus(downloadId, status)
                        }

                        DownloadStatus.FAILED -> {
                            failedTasks[task.id] = task
                            db.downloadDao.updateOnError(downloadId, status, error, time)
                        }

                        DownloadStatus.CANCEL -> db.downloadDao.updateStatus(downloadId, status)
                        else -> Unit
                    }
                }
            }

            updateTask?.let {
                if (isSendNotice) {
                    // commonMain 只分发状态，具体怎么展示由 Android 通知层自己决定。
                    when (it.status) {
                        DownloadStatus.PAUSED -> notificationDelegate.showPaused(it)
                        DownloadStatus.COMPLETED,
                        DownloadStatus.FAILED,
                        DownloadStatus.CANCEL -> {
                            notificationDelegate.showFinished(it)
                            cleanupTaskFiles(it)
                        }

                        else -> notificationDelegate.clear(it.id)
                    }
                }

                promoteAndExecute()
            }
        }
    }

    private fun cleanupTaskFiles(task: XyDownload) {
        try {
            // 临时文件始终删掉；最终文件只清理 0 字节占位文件，避免误删正常结果。
            DownloadPlatformFiles.deleteFile(task.tempFilePath)
            DownloadPlatformFiles.deleteFileIfEmpty(task.filePath)
        } catch (e: Exception) {
            DownloadLogger.d(DownloadConstants.LOG_TAG, e.toString())
        }
    }

    override fun close() {
        super.close()
        readyTasks.clear()
        runningTasks.clear()
        pausedTasks.clear()
        failedTasks.clear()
    }
}
