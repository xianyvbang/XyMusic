package cn.xybbz.config.download.core

import android.os.Environment
import cn.xybbz.di.ContextWrapper
import java.io.File

actual fun getDefaultDownloadDirectory(contextWrapper: ContextWrapper): String {
    val context = contextWrapper.context
    return  (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        ?: File(context.filesDir, DOWNLOAD_FILE_NAME)).absolutePath
}