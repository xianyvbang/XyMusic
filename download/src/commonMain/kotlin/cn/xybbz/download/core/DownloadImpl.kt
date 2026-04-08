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
import cn.xybbz.download.utils.FileNameResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class DownloadImpl(
    private val downloadDispatcher: DownloadDispatcherImpl,
    private val db: DownloadDatabaseClient,
) : IDownloader, DownloadIoScoped() {

    // 监听器只在模块内维护一份快照列表，避免把平台并发容器带进 commonMain。
    private val listeners = mutableListOf<DownloadListener>()

    init {
        createScope()
    }

    override suspend fun initData(mediaLibraryId: String?) {
        // 先把调度层的任务更新桥接给监听器，再做任务恢复。
        scope.launch {
            downloadDispatcher.taskUpdateEventFlow.collect { updatedTask ->
                notifyListeners(updatedTask)
            }
        }
        downloadDispatcher.rehydrate(mediaLibraryId)
    }

    override fun addListener(listener: DownloadListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    override fun removerListener(listener: DownloadListener) {
        listeners.remove(listener)
    }

    internal fun notifyListeners(task: XyDownload) {
        listeners.toList().forEach {
            it.onTaskUpdated(task)
        }
    }

    override suspend fun enqueue(vararg requests: DownloadRequest) {
        if (requests.isEmpty()) {
            return
        }
        val failTasks = mutableListOf<XyDownload>()
        val successTasksToInsert = mutableListOf<XyDownload>()

        for (request in requests) {
            var tempFilePath: String? = null
            try {
                // 最终文件路径在 commonMain 里只做解析，真正的临时文件创建交给平台 actual。
                val resolved = FileNameResolver.resolve(
                    request = request,
                    globalFinalDir = downloadDispatcher.config.finalDirectory,
                )
                tempFilePath = withContext(Dispatchers.IO) {
                    DownloadPlatformFiles.createTempDownloadFilePath(downloadDispatcher.contextWrapper)
                }

                if (!request.uid.isNullOrBlank()) {
                    val downloadTask = db.downloadDao.getMusicTaskByUid(
                        notTypeData = request.type,
                        uid = request.uid,
                        mediaLibraryId = request.mediaLibraryId
                    )
                    if (downloadTask != null) {
                        if (downloadTask.status != DownloadStatus.CANCEL &&
                            downloadTask.status != DownloadStatus.FAILED &&
                            downloadTask.status != DownloadStatus.PAUSED
                        ) {
                            continue
                        } else if (downloadTask.status == DownloadStatus.PAUSED) {
                            resume(downloadTask.id)
                            continue
                        }
                    }
                }

                // 这里只落库下载任务元数据，真正执行由 dispatcher 决定何时启动。
                val task = XyDownload(
                    url = request.url,
                    fileName = resolved.fileName,
                    filePath = resolved.finalPath,
                    fileSize = request.fileSize,
                    tempFilePath = tempFilePath,
                    typeData = request.type,
                    uid = request.uid,
                    title = request.title,
                    cover = request.cover,
                    duration = request.duration,
                    mediaLibraryId = request.mediaLibraryId,
                    extend = request.extend,
                    data = request.data,
                )

                successTasksToInsert.add(task)
            } catch (e: Throwable) {
                DownloadLogger.e(
                    DownloadConstants.LOG_TAG,
                    "Failed to prepare download for ${request.url}: ${e.message}",
                    e,
                )
                tempFilePath?.let(DownloadPlatformFiles::deleteFile)
                val time = Clock.System.now().toEpochMilliseconds()

                failTasks.add(
                    XyDownload(
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
                        mediaLibraryId = request.mediaLibraryId,
                        extend = request.extend,
                        data = request.data,
                        typeData = request.type
                    )
                )
            }
        }

        // 预处理失败的任务也直接入库，便于上层看到失败状态。
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

    override fun pauseAll() {
        downloadDispatcher.pauseAll()
    }

    override fun resume(vararg ids: Long) {
        if (ids.isNotEmpty()) downloadDispatcher.resume(ids.toList())
    }

    override fun cancel(vararg ids: Long) {
        if (ids.isNotEmpty()) downloadDispatcher.cancel(ids.toList())
    }

    override fun cancelAll() {
        downloadDispatcher.cancelAll()
    }

    override fun delete(vararg ids: Long, deleteFile: Boolean) {
        if (ids.isNotEmpty()) downloadDispatcher.delete(ids.toList(), deleteFile)
    }

    override fun updateConfig(config: DownloaderConfig) {
        downloadDispatcher.updateConfig(config)
    }

    override fun close() {
        pauseAll()
        super.close()
        downloadDispatcher.close()
    }
}
