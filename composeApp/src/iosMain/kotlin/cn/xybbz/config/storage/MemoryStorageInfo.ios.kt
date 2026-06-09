package cn.xybbz.config.storage

import cn.xybbz.platform.ContextWrapper
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient

actual fun getMemoryStorageInfo(
    contextWrapper: ContextWrapper,
    db: LocalDatabaseClient,
    downloadDb: DownloadDatabaseClient,
): MemoryStorageInfo = MemoryStorageInfo()

actual fun clearPlatformCache(contextWrapper: ContextWrapper) = Unit
