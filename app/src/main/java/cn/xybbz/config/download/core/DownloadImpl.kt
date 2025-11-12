package cn.xybbz.config.download.core

import android.content.Context
import android.util.Log
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.common.utils.FileNameResolver
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.enums.DownloadStatus
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class DownloadImpl(
    private val downloadDispatcher: DownloadDispatcherImpl,
    private val applicationContext: Context,
    private val db: DatabaseClient
) : IDownloader {

    val scope = CoroutineScopeUtils.getIo("downloadImpl")

    init {
        scope.launch{
            downloadDispatcher.rehydrate()

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
                val time = System.currentTimeMillis()

                val tempDir = File(applicationContext.cacheDir, "downloads")

                // 2. 确保这个稳定目录存在
                if (!tempDir.exists()) {
                    tempDir.mkdirs()
                }

                // 3. 在这个稳定目录中创建临时文件
                tempFile = File.createTempFile("download_", ".tmp", tempDir)

                val task = XyDownload(
                    url = request.url,
                    filePath = resolved.finalPath,
                    tempFilePath = tempFile.absolutePath,
                    fileName = resolved.fileName,
                    createTime = time,
                    updateTime = time,
                    uid = request.uid,
                    cover = request.cover,
                    duration = request.duration,
                    fileSize = request.fileSize,
                    title = request.title,
                    connectionId = request.connectionId
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