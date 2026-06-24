package cn.xybbz.download.core

import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.utils.deleteFileIfEmptyWithFileKit
import cn.xybbz.download.utils.deleteFileWithFileKit
import cn.xybbz.download.utils.fileLengthWithFileKit
import cn.xybbz.platform.ContextWrapper
import io.ktor.utils.io.ByteReadChannel

// 下载过程中和文件系统相关的能力统一从这里走 expect/actual，避免 commonMain 直接碰平台 API。
internal data class DownloadWriteResult(
    val status: DownloadStatus,
    val totalBytesWritten: Long,
)

internal object DownloadPlatformFiles {
    // 解析平台默认下载目录。
    fun defaultDownloadDirectory(contextWrapper: ContextWrapper): String =
        platformDefaultDownloadDirectoryPath(contextWrapper)

    // 创建下载使用的临时文件路径。
    fun createTempDownloadFilePath(contextWrapper: ContextWrapper): String =
        createPlatformTempDownloadFilePath(contextWrapper)

    fun fileLength(path: String): Long = fileLengthWithFileKit(path)

    // 查询指定路径所在磁盘当前可用空间。
    fun usableSpace(path: String, contextWrapper: ContextWrapper): Long =
        platformUsableSpace(path, contextWrapper)

    suspend fun deleteFile(path: String): Boolean = deleteFileWithFileKit(path)

    suspend fun deleteFileIfEmpty(path: String): Boolean = deleteFileIfEmptyWithFileKit(path)

    // 将网络响应持续写入文件，并在每个分片后回调上层决定是否继续；上下文和预期总大小用于写入中兜底校验磁盘空间。
    suspend fun writeResponseToFile(
        path: String,
        startOffset: Long,
        contextWrapper: ContextWrapper?,
        expectedTotalBytes: Long,
        source: ByteReadChannel,
        onChunkWritten: suspend (currentBytes: Long) -> DownloadStatus,
    ): DownloadWriteResult =
        writeResponseToPlatformFile(
            path = path,
            startOffset = startOffset,
            contextWrapper = contextWrapper,
            expectedTotalBytes = expectedTotalBytes,
            source = source,
            onChunkWritten = onChunkWritten,
        )
}

// 默认下载目录依赖平台上下文，只返回路径，目录创建由 commonMain 统一交给 FileKit。
internal expect fun platformDefaultDownloadDirectoryPath(contextWrapper: ContextWrapper): String

// 临时文件创建需要平台生成唯一文件名，但父目录创建统一通过 FileKit 处理。
internal expect fun createPlatformTempDownloadFilePath(contextWrapper: ContextWrapper): String

// 查询路径所在磁盘可用空间；平台层负责处理路径不存在时的父目录回退。
internal expect fun platformUsableSpace(path: String, contextWrapper: ContextWrapper): Long

// 断点续传需要 seek/setLength，FileKit sink 不能安全替代，仍由平台层实现随机写入。
internal expect suspend fun writeResponseToPlatformFile(
    path: String,
    startOffset: Long,
    contextWrapper: ContextWrapper?,
    expectedTotalBytes: Long,
    source: ByteReadChannel,
    onChunkWritten: suspend (currentBytes: Long) -> DownloadStatus,
): DownloadWriteResult
