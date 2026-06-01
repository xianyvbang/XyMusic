package cn.xybbz.download.core

import android.os.Environment
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.utils.ensureDirectoryWithFileKit
import cn.xybbz.download.utils.resolvePublicDownloadsSubdirectory
import cn.xybbz.platform.ContextWrapper
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readAvailable
import java.io.File
import java.io.RandomAccessFile

internal actual fun platformDefaultDownloadDirectoryPath(contextWrapper: ContextWrapper): String {
    val context = contextWrapper.context
    val publicDownloadsSubdirectory = resolvePublicDownloadsSubdirectory(contextWrapper)
    return (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        ?.let { File(it, publicDownloadsSubdirectory) }
        ?: File(context.filesDir, "downloads")).absolutePath
}

internal actual fun createPlatformTempDownloadFilePath(contextWrapper: ContextWrapper): String {
    val directory = File(contextWrapper.context.cacheDir, "xy-downloads")
    // 父目录创建统一走 FileKit，平台层只负责 Android 临时文件命名。
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

    // 调用方已经通过 flowOn(Dispatchers.IO) 保证这段阻塞文件 IO 运行在 IO 线程池。
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

            // 每个分片写完都把控制权还给上层，便于暂停/取消即时生效。
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
