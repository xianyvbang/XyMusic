package cn.xybbz.download.utils

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import cn.xybbz.platform.ContextWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException

// Android 端保留真实文件系统能力，包括占位文件预留和公开下载目录迁移。
actual object FileUtil {
    actual suspend fun reserveAvailableFileName(
        directory: String,
        fileName: String,
    ): String = withContext(Dispatchers.IO) {
        val targetDir = File(directory)
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw IOException("Failed to create target directory: ${targetDir.absolutePath}")
        }

        val tmpFileName = fileName.removePrefix(".")
        val originalFile = File(targetDir, tmpFileName)
        if (!originalFile.exists()) {
            originalFile.createNewFile()
            return@withContext tmpFileName
        }

        val nameWithoutExtension = tmpFileName.substringBeforeLast('.')
        val extension = tmpFileName.substringAfterLast('.', "")
        for (counter in 1..999) {
            val newFileName = if (extension.isNotEmpty()) {
                "${nameWithoutExtension}($counter).$extension"
            } else {
                "${nameWithoutExtension}($counter)"
            }

            val newFile = File(targetDir, newFileName)
            if (!newFile.exists()) {
                newFile.createNewFile()
                return@withContext newFileName
            }
        }

        throw IOException("Failed to find an available filename for '$fileName' after 999 attempts.")
    }

    actual suspend fun moveToPublicDirectory(
        contextWrapper: ContextWrapper?,
        sourcePath: String,
        finalPath: String,
        fileName: String,
    ): String = withContext(Dispatchers.IO) {
        val sourceFile = File(sourcePath)
        val placeholderFile = File(File(finalPath).parent ?: "", fileName)
        if (placeholderFile.exists()) {
            placeholderFile.delete()
        }

        contextWrapper ?: throw IllegalStateException(
            "Android moveToPublicDirectory requires ContextWrapper."
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            copyToPublicDownloads(contextWrapper, sourceFile, fileName)
        } else {
            moveToPublicLegacy(sourceFile, finalPath)
        }
    }

    /**
     * Android 10+ 使用 MediaStore 将文件复制到公开下载目录。
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun copyToPublicDownloads(
        contextWrapper: ContextWrapper,
        sourceFile: File,
        displayName: String,
    ): String {
        val publicDownloadsSubdirectory = resolvePublicDownloadsSubdirectory(contextWrapper)
        val contentResolver = contextWrapper.context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_DOWNLOADS}/$publicDownloadsSubdirectory",
            )
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IOException("Failed to create new MediaStore entry.")

        contentResolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(sourceFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        sourceFile.delete()
        return displayName
    }

    fun resolvePublicDownloadsSubdirectory(contextWrapper: ContextWrapper): String {
        val context = contextWrapper.context
        val appName = context.packageManager
            .getApplicationLabel(context.applicationInfo)
            .toString()
            .trim()

        return appName
            .ifBlank { context.packageName }
            .replace('/', '_')
            .replace('\\', '_')
    }

    /**
     * Android 9 及以下直接使用 File API 移动到目标路径。
     */
    private fun moveToPublicLegacy(sourceFile: File, finalPath: String): String {
        val finalFile = File(finalPath)
        finalFile.parentFile?.mkdirs()

        if (!sourceFile.renameTo(finalFile)) {
            sourceFile.copyTo(finalFile, overwrite = true)
            sourceFile.delete()
        }
        return finalFile.absolutePath
    }
}
