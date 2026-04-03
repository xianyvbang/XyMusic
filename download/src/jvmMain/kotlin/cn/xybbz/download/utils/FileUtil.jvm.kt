package cn.xybbz.download.utils

import cn.xybbz.platform.ContextWrapper

// JVM 端文件工具先留空，后续真正接桌面下载场景时再补实现。
actual object FileUtil {
    actual suspend fun reserveAvailableFileName(
        directory: String,
        fileName: String,
    ): String = fileName

    actual suspend fun moveToPublicDirectory(
        contextWrapper: ContextWrapper?,
        sourcePath: String,
        finalPath: String,
        fileName: String,
    ): String = finalPath
}
