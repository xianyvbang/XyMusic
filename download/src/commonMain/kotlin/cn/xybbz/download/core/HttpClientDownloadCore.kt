package cn.xybbz.download.core

import cn.xybbz.config.download.state.DownloadState
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.internal.DownloadCancellationException
import cn.xybbz.download.internal.DownloadLogger
import cn.xybbz.download.utils.FileUtil
import cn.xybbz.platform.ContextWrapper
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.timeout
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
            // 下载是长时间流式传输，不能受普通接口 requestTimeout 总耗时限制影响。
            timeout {
                requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            }
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

                // 如果服务端忽略 Range 并返回 200 OK，响应体就是完整文件。
                // 这种情况下必须从头覆盖临时文件，否则会把完整文件追加到旧半截后面。
                val writeStartOffset = if (
                    downloadedBytes > 0L &&
                    response.status == HttpStatusCode.OK
                ) {
                    DownloadLogger.d(
                        DownloadConstants.LOG_TAG,
                        "Server ignored Range for ${xyDownload.url}, restarting download from beginning.",
                    )
                    0L
                } else {
                    downloadedBytes
                }
                var currentBytes = writeStartOffset
                var lastUpdateTime = Clock.System.now().toEpochMilliseconds()
                var lastDownloadBytes = writeStartOffset
                // 任务记录里的总大小用于写入前和写入中的磁盘空间预估。
                val expectedTotalBytes = xyDownload.fileSize
                contextWrapper?.let {
                    // 服务端忽略 Range 时会从头覆盖写入，因此按完整文件大小重新校验。
                    val remainingBytes = if (response.status == HttpStatusCode.OK) {
                        expectedTotalBytes
                    } else {
                        (expectedTotalBytes - writeStartOffset).coerceAtLeast(0L)
                    }
                    // 真正打开文件流前先校验剩余下载量所需空间。
                    DownloadStorageGuard.ensureEnoughSpace(
                        path = xyDownload.tempFilePath,
                        needBytes = remainingBytes,
                        contextWrapper = it,
                    )
                }

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
                    startOffset = writeStartOffset,
                    contextWrapper = contextWrapper,
                    expectedTotalBytes = expectedTotalBytes,
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
                    // 临时文件通过 size 和可选 MD5 校验后，才能迁移到正式目录。
                    DownloadIntegrityValidator.verifyCompletedTempFile(
                        path = xyDownload.tempFilePath,
                        expectedBytes = xyDownload.fileSize,
                        expectedMd5 = xyDownload.expectedMd5,
                        writtenBytes = writeResult.totalBytesWritten,
                    )
                    // 下载完成后通过跨平台 FileUtil 迁移到最终目录。
                    val finalAbsolutePath = FileUtil.moveToPublicDirectory(
                        contextWrapper = contextWrapper,
                        sourcePath = xyDownload.tempFilePath,
                        finalPath = xyDownload.filePath,
                        fileName = xyDownload.fileName,
                        expectedBytes = xyDownload.fileSize,
                        expectedMd5 = xyDownload.expectedMd5,
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
