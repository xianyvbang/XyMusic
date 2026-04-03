package cn.xybbz.download.core

import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.platform.ContextWrapper
import io.ktor.utils.io.ByteReadChannel

// 下载过程中和文件系统相关的能力统一从这里走 expect/actual，避免 commonMain 直接碰平台 API。
internal data class DownloadWriteResult(
    val status: DownloadStatus,
    val totalBytesWritten: Long,
)

internal expect object DownloadPlatformFiles {
    // 解析平台默认下载目录。非 Android 端先保留空实现。
    fun defaultDownloadDirectory(contextWrapper: ContextWrapper? = null): String

    // 创建下载使用的临时文件路径。
    fun createTempDownloadFilePath(): String

    fun fileLength(path: String): Long
    fun deleteFile(path: String): Boolean
    fun deleteFileIfEmpty(path: String): Boolean

    // 将网络响应持续写入文件，并在每个分片后回调上层决定是否继续。
    suspend fun writeResponseToFile(
        path: String,
        startOffset: Long,
        source: ByteReadChannel,
        onChunkWritten: suspend (currentBytes: Long) -> DownloadStatus,
    ): DownloadWriteResult
}
