package cn.xybbz.download.core

import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.platform.ContextWrapper
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readAvailable
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

internal actual object DownloadPlatformFiles {
    actual fun defaultDownloadDirectory(contextWrapper: ContextWrapper): String {
        val targetDir = jvmDefaultDownloadDirectory(contextWrapper)
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw IOException("Failed to create download directory: ${targetDir.absolutePath}")
        }
        return targetDir.absolutePath
    }

    actual fun createTempDownloadFilePath(contextWrapper: ContextWrapper): String {
        val directory = jvmTempDownloadDirectory(contextWrapper)
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

        RandomAccessFile(targetFile, "rw").use { output ->
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
}

fun getJvmDownloadDirectory(contextWrapper: ContextWrapper): File {
    return jvmDefaultDownloadDirectory(contextWrapper)
}

fun getJvmDownloadTempDirectory(contextWrapper: ContextWrapper): File {
    return jvmTempDownloadDirectory(contextWrapper)
}

private fun jvmApplicationDirectory(contextWrapper: ContextWrapper): File {
    val appDir = contextWrapper.applicationDirectory
    if (!appDir.exists() && !appDir.mkdirs()) {
        throw IOException("Failed to create application directory: ${appDir.absolutePath}")
    }
    return appDir
}

private fun jvmDefaultDownloadDirectory(contextWrapper: ContextWrapper): File {
    return File(File(jvmApplicationDirectory(contextWrapper), "Downloads"), "XyMusic")
}

private fun jvmTempDownloadDirectory(contextWrapper: ContextWrapper): File {
    return File(File(jvmApplicationDirectory(contextWrapper), "temp"), "xy-downloads")
}
