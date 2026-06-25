package cn.xybbz.download.utils

import cn.xybbz.platform.ContextWrapper

// iOS 端普通文件迁移也走 FileKit，后续开启下载时不用再补一套复制/移动实现。
internal actual suspend fun moveToPublicDirectoryWithFileKit(
    contextWrapper: ContextWrapper?,
    sourcePath: String,
    finalPath: String,
    fileName: String,
    expectedBytes: Long,
    expectedMd5: String?,
): String = moveFileWithFileKit(
    sourcePath = sourcePath,
    finalPath = finalPath,
    expectedBytes = expectedBytes,
    expectedMd5 = expectedMd5,
)
