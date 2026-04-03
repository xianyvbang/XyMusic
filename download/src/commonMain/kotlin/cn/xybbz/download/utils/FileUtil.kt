package cn.xybbz.download.utils

import cn.xybbz.platform.ContextWrapper

// 文件系统相关能力下沉到平台 actual，commonMain 只保留统一入口。
expect object FileUtil {
    // 预留一个可用文件名，并在需要时创建占位文件，避免并发命名冲突。
    suspend fun reserveAvailableFileName(
        directory: String,
        fileName: String,
    ): String

    // 下载完成后将临时文件移动到最终目录；Android 会走公开下载目录逻辑。
    suspend fun moveToPublicDirectory(
        contextWrapper: ContextWrapper? = null,
        sourcePath: String,
        finalPath: String,
        fileName: String,
    ): String
}
