package cn.xybbz.download.utils

import cn.xybbz.platform.ContextWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

actual object FileUtil {
    private val reservationMutex = Mutex()

    actual suspend fun reserveAvailableFileName(
        directory: String,
        fileName: String,
    ): String = withContext(Dispatchers.IO) {
        reservationMutex.withLock {
            val targetDir = File(directory)
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                throw IOException("Failed to create target directory: ${targetDir.absolutePath}")
            }

            val sanitizedName = fileName.removePrefix(".").ifBlank { "download" }
            val baseName = sanitizedName.substringBeforeLast('.', sanitizedName)
            val extension = sanitizedName.substringAfterLast('.', "")

            fun reserve(candidate: String): String? {
                val file = File(targetDir, candidate)
                return if (file.createNewFile()) candidate else null
            }

            reserve(sanitizedName)?.let { return@withLock it }

            for (counter in 1..999) {
                val candidate = if (extension.isNotEmpty() && baseName.isNotBlank()) {
                    "$baseName($counter).$extension"
                } else if (extension.isNotEmpty() && baseName.isBlank()) {
                    "download($counter).$extension"
                } else {
                    "$baseName($counter)"
                }
                reserve(candidate)?.let { return@withLock it }
            }

            throw IOException("Failed to find an available filename for '$fileName' after 999 attempts.")
        }
    }

    actual suspend fun moveToPublicDirectory(
        contextWrapper: ContextWrapper?,
        sourcePath: String,
        finalPath: String,
        fileName: String,
    ): String = withContext(Dispatchers.IO) {
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists()) {
            throw IOException("Source file does not exist: $sourcePath")
        }

        val finalFile = File(finalPath)
        finalFile.parentFile?.let { parent ->
            if (!parent.exists() && !parent.mkdirs()) {
                throw IOException("Failed to create target directory: ${parent.absolutePath}")
            }
        }

        if (finalFile.exists() && finalFile.length() == 0L) {
            finalFile.delete()
        }

        if (!sourceFile.renameTo(finalFile)) {
            sourceFile.copyTo(finalFile, overwrite = true)
            if (!sourceFile.delete()) {
                throw IOException("Failed to delete temp file after copy: ${sourceFile.absolutePath}")
            }
        }

        finalFile.absolutePath
    }
}
