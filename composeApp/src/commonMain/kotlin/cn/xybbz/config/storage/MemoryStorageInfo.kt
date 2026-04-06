package cn.xybbz.config.storage

import cn.xybbz.platform.ContextWrapper
import cn.xybbz.localdata.config.LocalDatabaseClient

data class MemoryStorageInfo(
    val cacheSize: Long = 0L,
    val appDataSize: Long = 0L,
    val databaseSize: Long = 0L,
)

expect fun getMemoryStorageInfo(
    contextWrapper: ContextWrapper,
    db: LocalDatabaseClient,
): MemoryStorageInfo

expect fun clearPlatformCache(contextWrapper: ContextWrapper)
