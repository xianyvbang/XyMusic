package cn.xybbz.download.core

import cn.xybbz.config.download.state.DownloadState
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.internal.DownloadCancellationException
import cn.xybbz.download.internal.DownloadLogger
import cn.xybbz.download.utils.FileUtil
import cn.xybbz.platform.ContextWrapper
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.math.floor
import kotlin.time.Clock

class HttpClientDownloadCore : IDownloadCore {
    companion object {
        private const val UPDATE_PROGRESS_INTERVAL = 1000L
    }

    override suspend fun download(
        client: HttpClient,
        contextWrapper: ContextWrapper?,
        statusChange: suspend () -> DownloadStatus?,
        xyDownload: XyDownload,
    ): Flow<DownloadState> = flow {
        // 通过临时文件长度实现断点续传，commonMain 不直接接触平台文件类。
        val downloadedBytes = DownloadPlatformFiles.fileLength(xyDownload.tempFilePath)
        val request = client.prepareGet(xyDownload.url) {
            header("Range", "bytes=${downloadedBytes}-")
        }

        request.execute { response ->
            try {
                if (response.status != HttpStatusCode.OK &&
                    response.status != HttpStatusCode.PartialContent
                ) {
                    emit(DownloadState.Error("Response code error: ${response.status}"))
                    return@execute
                }

                var currentBytes = downloadedBytes
                var lastUpdateTime = Clock.System.now().toEpochMilliseconds()
                var lastDownloadBytes = downloadedBytes

                emit(
                    DownloadState.InProgress(
                        calculateProgress(currentBytes, xyDownload.fileSize),
                        currentBytes,
                        xyDownload.fileSize,
                        0L,
                    )
                )


                val writeResult = DownloadPlatformFiles.writeResponseToFile(
                    path = xyDownload.tempFilePath,
                    startOffset = downloadedBytes,
                    source = response.bodyAsChannel(),
                ) { writtenBytes ->
                    // 每个分片写入后在这里统一计算进度，并感知暂停/取消指令。
                    currentBytes = writtenBytes
                    val currentTime = Clock.System.now().toEpochMilliseconds()
                    val deltaTime = currentTime - lastUpdateTime
                    if (deltaTime >= UPDATE_PROGRESS_INTERVAL) {
                        val deltaBytes = currentBytes - lastDownloadBytes
                        val speedBps = if (deltaTime <= 0) 0L else deltaBytes * 1000 / deltaTime
                        emit(
                            DownloadState.InProgress(
                                calculateProgress(currentBytes, xyDownload.fileSize),
                                currentBytes,
                                xyDownload.fileSize,
                                speedBps,
                            )
                        )
                        lastUpdateTime = currentTime
                        lastDownloadBytes = currentBytes
                    }

                    statusChange() ?: DownloadStatus.DOWNLOADING
                }

                when (writeResult.status) {
                    DownloadStatus.PAUSED -> {
                        emit(DownloadState.Paused)
                        return@execute
                    }

                    DownloadStatus.CANCEL -> {
                        throw DownloadCancellationException("Download cancelled")
                    }

                    else -> Unit
                }

                try {
                    // 下载完成后通过跨平台 FileUtil 迁移到最终目录。
                    val finalAbsolutePath = FileUtil.moveToPublicDirectory(
                        contextWrapper = contextWrapper,
                        sourcePath = xyDownload.tempFilePath,
                        finalPath = xyDownload.filePath,
                        fileName = xyDownload.fileName,
                    )
                    emit(DownloadState.Success)
                    DownloadLogger.d(DownloadConstants.LOG_TAG, "finalAbsolutePath:$finalAbsolutePath")
                } catch (e: Exception) {
                    emit(
                        DownloadState.Error(
                            "Failed to move temp file to final destination: ${e.message}",
                            e,
                        )
                    )
                }
            } catch (e: DownloadCancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                emit(DownloadState.Error("Download failed: ${e.message ?: "unknown error"}", e))
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun calculateProgress(downloaded: Long, total: Long): Float {
        return if (total > 0) floor((downloaded * 1000.0) / total).toFloat() / 10f else 0f
    }
}
