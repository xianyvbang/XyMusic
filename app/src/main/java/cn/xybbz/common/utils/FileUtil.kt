package cn.xybbz.common.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object FileUtil {

    suspend fun moveToPublicDirectory(
        context: Context,
        sourceFile: File,
        finalPath: String,
        fileName: String,
    ): String = withContext(Dispatchers.IO) {
        // 在移动之前，先找到并删除对应的隐藏占位文件
        val placeholderFile = File(File(finalPath).parent, fileName)
        if (placeholderFile.exists()) {
            placeholderFile.delete()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ -> Use MediaStore API
            copyToPublicDownloads(context, sourceFile, fileName)
        } else {
            // Android 9 and below -> Use legacy File API
            moveToPublicLegacy(sourceFile, finalPath)
        }
    }


    /**
     * For Android 10+ 使用MediaStore将文件复制移动到公开目录
     * @return 文件的最新路径
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    @Throws(IOException::class)
    private fun copyToPublicDownloads(
        context: Context,
        sourceFile: File,
        displayName: String,
    ): String {
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            // 你可以根据文件类型设置 MIME_TYPE
//            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IOException("Failed to create new MediaStore entry.")

        contentResolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(sourceFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return displayName // 在 MediaStore 中，我们通常只关心文件名
    }

    /**
     * For Android 9 and below.使用File的Api将文件移动到新目录
     * @return 文件的最新路径
     */
    @Throws(IOException::class)
    private fun moveToPublicLegacy(sourceFile: File, finalPath: String): String {
        val finalFile = File(finalPath)
        finalFile.parentFile?.mkdirs()

        if (!sourceFile.renameTo(finalFile)) {
            // Fallback to copy+delete
            sourceFile.copyTo(finalFile, overwrite = true)
        }
        return finalFile.absolutePath
    }
}