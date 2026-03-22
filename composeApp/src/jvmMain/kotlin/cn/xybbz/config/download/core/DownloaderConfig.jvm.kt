package cn.xybbz.config.download.core

import cn.xybbz.di.ContextWrapper
import java.io.File

actual fun getDefaultDownloadDirectory(contextWrapper: ContextWrapper): String {
    return  File(System.getProperty("java.io.tmpdir"), DOWNLOAD_FILE_NAME).path
}