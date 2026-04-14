package cn.xybbz.download.core

import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.platform.ContextWrapper
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

internal actual object DownloadPlatformFiles {
    private fun applicationDirectory(): File {
        val appDirPath = System.getProperty("user.dir").orEmpty().ifBlank { "." }
        val appDir = File(appDirPath).absoluteFile
        if (!appDir.exists() && !appDir.mkdirs()) {
            throw IOException("Failed to create application directory: ${appDir.absolutePath}")
        }
        return appDir
    }

    actual fun defaultDownloadDirectory(contextWrapper: ContextWrapper?): String {
        val targetDir = File(File(applicationDirectory(), "Downloads"), "XyMusic")

        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw IOException("Failed to create download directory: ${targetDir.absolutePath}")
        }
        return targetDir.absolutePath
    }

    actual fun createTempDownloadFilePath(contextWrapper: ContextWrapper): String {
        val directory = File(File(applicationDirectory(), "temp"), "xy-downloads")
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Failed to create temp directory: ${directory.absolutePath}")
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

    actual suspend fun writeResponseToFile(
        path: String,
        startOffset: Long,
        source: ByteReadChannel,
        onChunkWritten: suspend (currentBytes: Long) -> DownloadStatus,
    ): DownloadWriteResult = withContext(Dispatchers.IO) {
        val targetFile = File(path)
        targetFile.parentFile?.mkdirs()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var currentBytes = startOffset

        RandomAccessFile(targetFile, "rw").use { output ->
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

                when (onChunkWritten(currentBytes)) {
                    DownloadStatus.PAUSED ->
                        return@withContext DownloadWriteResult(DownloadStatus.PAUSED, currentBytes)

                    DownloadStatus.CANCEL ->
                        return@withContext DownloadWriteResult(DownloadStatus.CANCEL, currentBytes)

                    else -> Unit
                }
            }
        }

        DownloadWriteResult(DownloadStatus.COMPLETED, currentBytes)
    }
}
