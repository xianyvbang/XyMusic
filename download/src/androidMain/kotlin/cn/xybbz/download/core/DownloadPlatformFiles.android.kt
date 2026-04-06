package cn.xybbz.download.core

import android.os.Environment
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.platform.ContextWrapper
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import java.io.File
import java.io.RandomAccessFile

// Android 端继续沿用本地文件系统实现，把 commonMain 需要的行为补齐成 actual。
internal actual object DownloadPlatformFiles {
    actual fun defaultDownloadDirectory(contextWrapper: ContextWrapper?): String {
        val context = contextWrapper?.context ?: return ""
        return (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "downloads")).absolutePath
    }

    actual fun createTempDownloadFilePath(): String {
        val directory = File(System.getProperty("java.io.tmpdir"), "xy-downloads")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return File.createTempFile("download_", ".tmp", directory).absolutePath
    }

    actual fun fileLength(path: String): Long {
        if (path.isBlank()) {
            return 0L
        }
        val file = File(path)
        return if (file.exists()) file.length() else 0L
    }

    actual fun deleteFile(path: String): Boolean {
        if (path.isBlank()) {
            return false
        }
        val file = File(path)
        return file.exists() && file.delete()
    }

    actual fun deleteFileIfEmpty(path: String): Boolean {
        if (path.isBlank()) {
            return false
        }
        val file = File(path)
        return file.exists() && file.length() == 0L && file.delete()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    actual suspend fun writeResponseToFile(
        path: String,
        startOffset: Long,
        source: ByteReadChannel,
        onChunkWritten: suspend (currentBytes: Long) -> DownloadStatus,
    ): DownloadWriteResult {
        val targetFile = File(path)
        targetFile.parentFile?.mkdirs()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var currentBytes = startOffset

        // 调用方已经通过 flowOn(Dispatchers.IO) 保证这段阻塞文件 IO 运行在 IO 线程池。
        RandomAccessFile(targetFile, "rw").use { output ->
            // 从断点位置继续写，保持和旧 Android 下载逻辑一致。
            output.seek(startOffset)
            while (true) {
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
}
