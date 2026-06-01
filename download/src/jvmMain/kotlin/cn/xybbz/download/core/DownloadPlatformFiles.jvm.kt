package cn.xybbz.download.core

import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.utils.ensureDirectoryWithFileKit
import cn.xybbz.platform.ContextWrapper
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readAvailable
import java.io.File
import java.io.RandomAccessFile

internal actual fun platformDefaultDownloadDirectoryPath(contextWrapper: ContextWrapper): String {
    return File(File(contextWrapper.applicationDirectory, "Downloads"), "XyMusic").absolutePath
}

internal actual fun createPlatformTempDownloadFilePath(contextWrapper: ContextWrapper): String {
    val directory = File(File(contextWrapper.applicationDirectory, "temp"), "xy-downloads")
    // 父目录创建统一走 FileKit，平台层只负责调用 JVM 临时文件命名能力。
    ensureDirectoryWithFileKit(directory.absolutePath)
    return File.createTempFile("download_", ".tmp", directory).absolutePath
}

@Suppress("BlockingMethodInNonBlockingContext")
internal actual suspend fun writeResponseToPlatformFile(
    path: String,
    startOffset: Long,
    source: ByteReadChannel,
    onChunkWritten: suspend (currentBytes: Long) -> DownloadStatus,
): DownloadWriteResult {
    val targetFile = File(path)
    targetFile.parentFile?.absolutePath?.let(::ensureDirectoryWithFileKit)
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var currentBytes = startOffset

    RandomAccessFile(targetFile, "rw").use { output ->
        // 断点续传必须裁剪旧尾部并 seek 到断点，FileKit sink 不能替代这类随机写入。
        output.setLength(startOffset)
        output.seek(startOffset)

        while (!source.exhausted()) {
            val bytesRead = source.readAvailable(buffer, 0, buffer.size)
            if (bytesRead == -1) {
                break
            }
            if (bytesRead == 0) {
                continue
            }

            output.write(buffer, 0, bytesRead)
            currentBytes += bytesRead

            when (onChunkWritten(currentBytes)) {
                DownloadStatus.PAUSED ->
                    return DownloadWriteResult(DownloadStatus.PAUSED, currentBytes)

                DownloadStatus.CANCEL ->
                    return DownloadWriteResult(DownloadStatus.CANCEL, currentBytes)

                else -> Unit
            }
        }
    }

    return DownloadWriteResult(DownloadStatus.COMPLETED, currentBytes)
}

fun getJvmDownloadDirectory(contextWrapper: ContextWrapper): File {
    return File(platformDefaultDownloadDirectoryPath(contextWrapper))
}

fun getJvmDownloadTempDirectory(contextWrapper: ContextWrapper): File {
    return File(File(contextWrapper.applicationDirectory, "temp"), "xy-downloads")
}
