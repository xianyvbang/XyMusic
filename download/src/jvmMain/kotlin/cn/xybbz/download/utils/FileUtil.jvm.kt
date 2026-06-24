package cn.xybbz.download.utils

import cn.xybbz.platform.ContextWrapper

// JVM 端普通文件迁移直接委托 FileKit，保留同名 actual 作为平台出口。
internal actual suspend fun moveToPublicDirectoryWithFileKit(
    contextWrapper: ContextWrapper?,
    sourcePath: String,
    finalPath: String,
    fileName: String,
): String = moveFileWithFileKit(
    sourcePath = sourcePath,
    finalPath = finalPath,
    contextWrapper = contextWrapper,
)
