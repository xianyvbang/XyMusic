package cn.xybbz.config.download.core

import android.content.Context
import android.icu.math.BigDecimal
import android.util.Log
import cn.xybbz.api.client.ApiConfig
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.exception.CancelDownloadException
import cn.xybbz.common.utils.FileUtil
import cn.xybbz.download.core.IDownloadCore
import cn.xybbz.download.state.DownloadState
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.enums.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.awaitResponse
import java.io.File
import java.io.RandomAccessFile
import java.math.RoundingMode
import kotlin.coroutines.cancellation.CancellationException

class OkhttpDownloadCore(
    private val context: Context
) : IDownloadCore {
    companion object {
        private const val UPDATE_PROGRESS_INTERVAL = 1000L
    }

    /**
     * 下载
     * @param [url] 网址
     * @param [client] okhttp客户端
     * @param [statusChange] 状态变更信息
     */
    override suspend fun download(
        client: ApiConfig,
        statusChange: suspend () -> DownloadStatus?,
        xyDownload: XyDownload
    ): Flow<DownloadState> = flow {
        val tempFile = File(xyDownload.tempFilePath)
        val downloadedBytes = if (tempFile.exists()) tempFile.length() else 0L
        val call = client.downloadApi()
            .downloadFile(
                fileUrl = xyDownload.url,
                range = "bytes=${downloadedBytes}-"
            )

        val response = try {
            call.awaitResponse()
        } catch (e: Exception) {
            emit(DownloadState.Error("下载失败: ${e.message}", e))
            return@flow
        }

        if (!response.isSuccessful && response.code() != 206) {
            emit(DownloadState.Error("响应码错误: ${response.code()}"))
            return@flow
        }

        var currentBytes = downloadedBytes
        var lastUpdateTime = System.currentTimeMillis()
        var lastDownLoadSpeed = downloadedBytes

        emit(
            DownloadState.InProgress(
                calculateProgress(currentBytes, xyDownload.fileSize),
                currentBytes,
                xyDownload.fileSize, 0
            )
        )

        response.body()?.let { body ->
            val buffer = ByteArray(8 * 1024)
            var bytes: Int
            RandomAccessFile(tempFile, "rw").use { output ->

                output.seek(downloadedBytes)

                while (body.byteStream().read(buffer).also { bytes = it } != -1) {
                    output.write(buffer, 0, bytes)
                    currentBytes += bytes

                    val currentTime = System.currentTimeMillis()
                    //上次更新间隔
                    val deltaTime = currentTime - lastUpdateTime
                    //限制频率
                    if (deltaTime >= UPDATE_PROGRESS_INTERVAL) {
                        val deltaBytes = currentBytes - lastDownLoadSpeed
                        val speedBps = deltaBytes * 1000 / deltaTime
                        emit(
                            DownloadState.InProgress(
                                calculateProgress(currentBytes, xyDownload.fileSize),
                                currentBytes,
                                xyDownload.fileSize, speedBps
                            )
                        )
                        lastUpdateTime = currentTime
                        lastDownLoadSpeed = currentBytes
                    }

                    //判断外部传入的状态,是取消还是暂停
                    val currentStatus = statusChange.invoke()
                    if (currentStatus == DownloadStatus.PAUSED) {
                        // 如果是暂停,则暂停读取
                        emit(DownloadState.Paused)
                        return@flow
                    }
                    if (currentStatus == DownloadStatus.CANCEL) {
                        // 感知到取消指令，抛出异常以终止流程
                        call.cancel()
                        throw CancelDownloadException("取消下载")
                    }
                }
            }

        }

        try {
            val finalAbsolutePath = FileUtil.moveToPublicDirectory(
                context = context,
                sourceFile = tempFile,
                finalPath = xyDownload.filePath,
                fileName = xyDownload.fileName,
            )
            emit(DownloadState.Success)
            Log.d(Constants.LOG_ERROR_PREFIX, "finalAbsolutePath:$finalAbsolutePath")
        } catch (e: Exception) {
            emit(
                DownloadState.Error(
                    "Failed to move temp file to final destination: ${e.message}",
                    e
                )
            )
        }

    }.flowOn(Dispatchers.IO)

    private fun calculateProgress(downloaded: Long, total: Long): Float {
        return if (total > 0) ((downloaded * 100.0) / total).roundToFloat() else 0f
    }

    fun Double.roundToFloat(scale: Int = 1): Float {
        return BigDecimal(this)
            .setScale(scale, RoundingMode.DOWN.ordinal)
            .toFloat()
    }
}