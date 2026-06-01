package cn.xybbz.download.core

import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.platform.ContextWrapper
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.filesDir
import io.ktor.utils.io.ByteReadChannel

internal actual fun platformDefaultDownloadDirectoryPath(contextWrapper: ContextWrapper): String {
    val directory = PlatformFile(FileKit.filesDir, "Downloads")
    directory.createDirectories()
    return directory.absolutePath()
}

internal actual fun createPlatformTempDownloadFilePath(contextWrapper: ContextWrapper): String {
    val directory = PlatformFile(FileKit.cacheDir, "xy-downloads")
    directory.createDirectories()
    // iOS 下载调度当前未启用；这里先给出 FileKit 目录下的稳定临时路径入口。
    return PlatformFile(directory, "download_${kotlin.time.Clock.System.now().toEpochMilliseconds()}.tmp")
        .absolutePath()
}

internal actual suspend fun writeResponseToPlatformFile(
    path: String,
    startOffset: Long,
    source: ByteReadChannel,
    onChunkWritten: suspend (currentBytes: Long) -> DownloadStatus,
): DownloadWriteResult {
    throw NotImplementedError("Download random-write APIs are not implemented on iOS yet.")
}
