package cn.xybbz.config.download.core

import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.download.work.DownloadWork
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.enums.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue


class DownloadDispatcherImpl(
    private val db: DatabaseClient,
    private val workManager: WorkManager,
    var config: DownloaderConfig,
    private val connectionConfigServer: ConnectionConfigServer
) : IDownloadDispatcher {

    val scope = CoroutineScopeUtils.getIo("download")
    private val globalMutex = Mutex()

    private val readyTasks = ConcurrentLinkedQueue<XyDownload>()
    private val runningTasks = ConcurrentHashMap<Long, XyDownload>()
    private val pausedTasks = ConcurrentHashMap<Long, XyDownload>()

    private var lastDbUpdateTime = 0L
    private val dbUpdateInterval = 1000L


    /**
     * 初始化加载数据库中的信息
     * todo 这里需要修改,改为初始化所有数据都是暂停状态
     */
    suspend fun rehydrate() = withContext(Dispatchers.IO) {
        // Only rehydrate once
        if (readyTasks.isNotEmpty() || runningTasks.isNotEmpty() || pausedTasks.isNotEmpty()) return@withContext

        val allTasks = db.downloadDao.getAllTasksSuspend(connectionConfigServer.getConnectionId())
        allTasks.forEach { task ->
            when (task.status) {
                DownloadStatus.QUEUED -> readyTasks.add(task)

                DownloadStatus.DOWNLOADING -> {
                    task.status = DownloadStatus.QUEUED
                    readyTasks.add(task)
                }

                DownloadStatus.PAUSED -> pausedTasks[task.id] = task
                else -> {
                }
            }
        }
        promoteAndExecute()
    }

    fun enqueue(newTasks: List<XyDownload>) {
        scope.launch {
            readyTasks.addAll(newTasks)
            promoteAndExecute()
        }
    }

    fun updateConfig(newConfig: DownloaderConfig) {
        this.config = newConfig
        // Trigger a promotion check in case the concurrency limit was increased
        promoteAndExecute()
    }

    fun pause(ids: List<Long>) {
        scope.launch {
            ids.forEach { id ->
                var task: XyDownload? = null
                readyTasks.find { it.id == id }?.let {
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
                    // Key Node: Persist state change
                    db.downloadDao.updateStatus(it.id, DownloadStatus.QUEUED)
                }
            }
            promoteAndExecute()
        }
    }

    fun cancel(ids: List<Long>) {
        scope.launch {
            val time = System.currentTimeMillis()
            val tasksToCancel = mutableListOf<XyDownload>()
            ids.forEach { id ->
                readyTasks.find { it.id == id }
                    ?.also { readyTasks.remove(it); tasksToCancel.add(it) }
                runningTasks.remove(id)?.also { tasksToCancel.add(it) }
                pausedTasks.remove(id)?.also { tasksToCancel.add(it) }
            }

            if (tasksToCancel.isNotEmpty()) {
                val taskIds = tasksToCancel.map { it.id }
                // Key Node: Persist state change
                db.downloadDao.updateStatuses(taskIds, DownloadStatus.CANCEL, time)

                tasksToCancel.forEach { task ->
                    workManager.cancelAllWorkByTag(task.id.toString())
                    try {
                        File(task.tempFilePath).delete()
                        cleanupTaskFiles(task)
                    } catch (e: Exception) {
                        Log.d(Constants.LOG_ERROR_PREFIX, "$e")
                    }
                }
            }
            promoteAndExecute()
        }
    }

    fun delete(ids: List<Long>, deleteFile: Boolean) {
        scope.launch {
            val tasksToCancel = mutableListOf<XyDownload>()
            ids.forEach { id ->
                readyTasks.find { it.id == id }
                    ?.also { readyTasks.remove(it); tasksToCancel.add(it) }
                runningTasks.remove(id)?.also { tasksToCancel.add(it) }
                pausedTasks.remove(id)?.also { tasksToCancel.add(it) }
            }

            if (tasksToCancel.isNotEmpty()) {
                tasksToCancel.forEach { task ->
                    workManager.cancelAllWorkByTag(task.id.toString())
                    try {
                        cleanupTaskFiles(task)
                        if (deleteFile) File(task.filePath).delete()
                    } catch (e: Exception) {
                        Log.d(Constants.LOG_ERROR_PREFIX, "$e")
                    }
                }
            }
            db.downloadDao.deleteById(*ids.toLongArray())
            promoteAndExecute()
        }
    }

    private fun promoteAndExecute() = scope.launch {
        globalMutex.withLock {
            while (runningTasks.size < config.maxConcurrentDownloads) {
                val task = readyTasks.poll() ?: break // No more tasks to run

                task.status = DownloadStatus.DOWNLOADING
                runningTasks[task.id] = task
                startDownloadWorker(task)
            }
        }
    }

    private suspend fun startDownloadWorker(task: XyDownload) {
        // Key Node: Persist state change just before starting
        db.downloadDao.updateStatus(task.id, DownloadStatus.DOWNLOADING)
        val workRequest = OneTimeWorkRequestBuilder<DownloadWork>()
            .setInputData(workDataOf(Constants.DOWNLOAD_ID to task.id))
            .addTag(task.id.toString())
            .build()

        workManager.enqueueUniqueWork(
            task.id.toString(),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }


    override fun onProgress(
        downloadId: Long,
        progress: Int,
        downloadedBytes: Long,
        totalBytes: Long,
        speedBps: Long
    ) {

        //更新map的数据
        val download = runningTasks[downloadId]
        download?.let {
            it.progress = progress
            it.downloadedBytes = downloadedBytes
            it.totalBytes = totalBytes
        }
        val currentTime = System.currentTimeMillis()
        //更新数据库数据
        scope.launch {
            if (currentTime - lastDbUpdateTime >= dbUpdateInterval) {
                lastDbUpdateTime = currentTime
                scope.launch(Dispatchers.IO) {
                    // 批量更新所有正在运行任务的进度
                    runningTasks.values.forEach { runningTask ->
                        db.downloadDao.updateProgress(
                            runningTask.id,
                            runningTask.progress,
                            runningTask.downloadedBytes,
                            runningTask.totalBytes,
                            currentTime
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
        error: String?
    ) {
        scope.launch {
            var updateTask: XyDownload? = null

            globalMutex.withLock {
                val time = System.currentTimeMillis()
                val task = runningTasks.remove(downloadId)
                // 如果任务是从 runningTasks 中移除的，才进行后续操作
                if (task != null) {
                    updateTask = task
                    task.status = status
                    // 使用更全面的数据库更新方法
                    when (status) {
                        DownloadStatus.COMPLETED -> {
                            if (finalPath != null) {
                                db.downloadDao.updateOnSuccess(
                                    downloadId,
                                    status,
                                    finalPath,
                                    time
                                ) // 假设 repo 方法已更新
                            }
                        }

                        DownloadStatus.PAUSED -> db.downloadDao.updateStatus(downloadId, status)
                        DownloadStatus.FAILED -> db.downloadDao.updateOnError(
                            downloadId,
                            status,
                            error,
                            time
                        )

                        DownloadStatus.CANCEL -> db.downloadDao.updateStatus(downloadId, status)
                        else -> {}
                    }
                }
            }

            updateTask?.let {
                promoteAndExecute()
            }
        }

    }

    private fun cleanupTaskFiles(task: XyDownload) {
        try {
            // 清理临时文件
            val tempFile = File(task.tempFilePath)
            if (tempFile.exists()) {
                tempFile.delete()
            }

            // 清理最终目录中的占位文件
            val finalFile = File(task.filePath)
            if (finalFile.exists() && finalFile.length() == 0L) { // 只删除 0 字节的占位文件，以防万一
                finalFile.delete()
            }
        } catch (e: Exception) {
            Log.d(Constants.LOG_ERROR_PREFIX, "$e")
        }
    }
}