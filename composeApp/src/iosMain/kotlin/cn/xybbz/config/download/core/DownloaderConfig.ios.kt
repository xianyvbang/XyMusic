package cn.xybbz.config.download.core

import cn.xybbz.di.ContextWrapper
import cn.xybbz.localdata.config.documentDirectory

actual fun getDefaultDownloadDirectory(contextWrapper: ContextWrapper): String {
    return documentDirectory() + "/${DOWNLOAD_FILE_NAME}"
}

