package cn.xybbz.config.download.core

import android.content.Context
import android.util.Log
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.common.utils.FileNameResolver
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.enums.DownloadStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

class DownloadImpl(
    private val downloadDispatcher: DownloadDispatcherImpl,
    private val applicationContext: Context,
    private val db: DatabaseClient,
) : IDownloader {

    val scope = CoroutineScopeUtils.getIo("downloadImpl")
    private val listeners = CopyOnWriteArrayList<DownloadListener>()
    private var taskUpdateJob: Job? = null
    override fun initData() {
        observeLoginSuccessForDispatcher()
    }

    private fun observeLoginSuccessForDispatcher() {
        scope.launch {
            downloadDispatcher.connectionConfigServer.loginSuccessEvent.collect {
                startTaskUpdateObserver()
            }
        }
    }

    private fun startTaskUpdateObserver() {
        // 取消之前的 Job，避免重复监听
        taskUpdateJob?.cancel()

        // 重新订阅 Dispatcher 更新事件
        taskUpdateJob = scope.launch {
            downloadDispatcher.rehydrate()
            downloadDispatcher.taskUpdateEventFlow.collect { updatedTask ->
                notifyListeners(updatedTask)
            }
        }
    }

    /**
     * Adds a listener to receive download task updates.
     * @param listener The listener to add.
     */
    override fun addListener(listener: DownloadListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    /**
     * Removes a previously added listener.
     * @param listener The listener to remove.
     */
    override fun removerListener(listener: DownloadListener) {
        listeners.remove(listener)
    }

    /**
     * Internal method to notify all registered listeners about a task update.
     */
    internal fun notifyListeners(task: XyDownload) {
        // CopyOnWriteArrayList 使得遍历操作是线程安全的
        listeners.forEach {
            it.onTaskUpdated(task)
        }
    }

    override suspend fun enqueue(vararg requests: DownloadRequest) {
        if (requests.isEmpty()) {
            return
        }
        val failTasks = mutableListOf<XyDownload>()
        val successTasksToInsert = mutableListOf<XyDownload>()
        var tempFile: File? = null
        // 1. 准备阶段：创建所有 Task 实体
        for (request in requests) {
            try {
                val resolved = FileNameResolver.resolve(
                    request = request,
                    globalFinalDir = downloadDispatcher.config.finalDirectory,
                )
                val tempDir = File(applicationContext.cacheDir, "downloads")

                // 2. 确保这个稳定目录存在
                if (!tempDir.exists()) {
                    tempDir.mkdirs()
                }

                // 3. 在这个稳定目录中创建临时文件
                tempFile = File.createTempFile("download_", ".tmp", tempDir)

                if (!request.uid.isNullOrBlank()) {
                    val downloadTask = db.downloadDao.getMusicTaskByUid(uid = request.uid)
                    if (downloadTask != null) {
                        if (downloadTask.status != DownloadStatus.CANCEL && downloadTask.status != DownloadStatus.FAILED && downloadTask.status != DownloadStatus.PAUSED) {
                            continue
                        } else if (downloadTask.status == DownloadStatus.PAUSED) {
                            resume(downloadTask.id)
                            continue
                        }
                    }
                }

                val task = XyDownload(
                    url = request.url,
                    fileName = resolved.fileName,
                    filePath = resolved.finalPath,
                    fileSize = request.fileSize,
                    tempFilePath = tempFile.absolutePath,
                    typeData = request.type,
                    uid = request.uid,
                    title = request.title,
                    cover = request.cover,
                    duration = request.duration,
                    connectionId = request.connectionId,
                    music = request.music
                )

                successTasksToInsert.add(task)
            } catch (e: IOException) {
                // --- 捕获异常，创建“失败”任务 ---
                // 1. 记录详细错误
                Log.e(
                    "Downloader",
                    "Failed to resolve/reserve filename for ${request.url}: ${e.message}",
                    e
                )
                tempFile?.delete()
                val time = System.currentTimeMillis()

                // 2. 创建一个状态为 FAILED 的任务实体
                val failedTask = XyDownload(
                    url = request.url,
                    filePath = "",
                    tempFilePath = "",
                    fileName = request.fileName.ifBlank { "unknown file" },
                    status = DownloadStatus.FAILED,
                    createTime = time,
                    updateTime = time,
                    uid = request.uid,
                    cover = request.cover,
                    duration = request.duration,
                    fileSize = request.fileSize,
                    title = request.title,
                    connectionId = request.connectionId
                )
                failTasks.add(failedTask)
            }
        }
        // 2. 将预处理失败的任务直接插入数据库
        if (failTasks.isNotEmpty()) {
            db.downloadDao.insert(*failTasks.toTypedArray())
        }
        // 3. 持久化：将新任务批量写入数据库
        if (successTasksToInsert.isNotEmpty()) {
            //todo 这里需要判断是否为重复下载,如果是重复下载则只是更新数据
            val successIds = db.downloadDao.insert(*successTasksToInsert.toTypedArray())

            // 4. 根据新 ID 从数据库重新获取完整的、带有正确 ID 的任务对象
            val newTasksWithCorrectIds = db.downloadDao.getByIds(successIds)

            // 5. 委托：将带有正确 ID 的任务交给 Dispatcher 处理
            if (newTasksWithCorrectIds.isNotEmpty()) {
                downloadDispatcher.enqueue(newTasksWithCorrectIds)
            }
        }
    }

    override fun pause(vararg ids: Long) {
        if (ids.isNotEmpty()) downloadDispatcher.pause(ids.toList())
    }

    override fun resume(vararg ids: Long) {
        if (ids.isNotEmpty()) downloadDispatcher.resume(ids.toList())
    }

    override fun cancel(vararg ids: Long) {
        if (ids.isNotEmpty()) downloadDispatcher.cancel(ids.toList())
    }

    override fun delete(vararg ids: Long, deleteFile: Boolean) {
        if (ids.isNotEmpty()) downloadDispatcher.delete(ids.toList(), deleteFile)
    }

    override fun updateConfig(config: DownloaderConfig) {
        downloadDispatcher.updateConfig(config)
    }
}