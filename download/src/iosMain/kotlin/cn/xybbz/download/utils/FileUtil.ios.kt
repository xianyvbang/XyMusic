package cn.xybbz.download.utils

import cn.xybbz.platform.ContextWrapper

// iOS 端文件工具先留空，当前只保证跨平台结构完整。
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
