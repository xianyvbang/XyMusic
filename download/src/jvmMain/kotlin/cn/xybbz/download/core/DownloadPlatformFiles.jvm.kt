package cn.xybbz.download.core

import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.platform.ContextWrapper
import io.ktor.utils.io.ByteReadChannel

// JVM 端先留空，等后续真正接入桌面下载能力时再补实现。
internal actual object DownloadPlatformFiles {
    actual fun defaultDownloadDirectory(contextWrapper: ContextWrapper?): String = ""

    actual fun createTempDownloadFilePath(contextWrapper: ContextWrapper): String = ""

    actual fun fileLength(path: String): Long = 0L

    actual fun deleteFile(path: String): Boolean = false

    actual fun deleteFileIfEmpty(path: String): Boolean = false

    actual suspend fun writeResponseToFile(
        path: String,
        startOffset: Long,
        source: ByteReadChannel,
        onChunkWritten: suspend (currentBytes: Long) -> DownloadStatus,
    ): DownloadWriteResult {
        throw NotImplementedError("Download file APIs are not implemented on JVM yet.")
    }
}
