package cn.xybbz.config.storage

import cn.xybbz.platform.ContextWrapper
import cn.xybbz.localdata.config.LocalDatabaseClient

actual fun getMemoryStorageInfo(
    contextWrapper: ContextWrapper,
    db: LocalDatabaseClient,
): MemoryStorageInfo = MemoryStorageInfo()

actual fun clearPlatformCache(contextWrapper: ContextWrapper) = Unit
