package cn.xybbz.config.storage

import cn.xybbz.di.ContextWrapper
import cn.xybbz.localdata.config.DatabaseClient

actual fun getMemoryStorageInfo(
    contextWrapper: ContextWrapper,
    db: DatabaseClient,
): MemoryStorageInfo = MemoryStorageInfo()

actual fun clearPlatformCache(contextWrapper: ContextWrapper) = Unit
