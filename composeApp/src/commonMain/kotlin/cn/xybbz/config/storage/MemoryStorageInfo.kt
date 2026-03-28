package cn.xybbz.config.storage

import cn.xybbz.di.ContextWrapper
import cn.xybbz.localdata.config.DatabaseClient

data class MemoryStorageInfo(
    val cacheSize: Long = 0L,
    val appDataSize: Long = 0L,
    val databaseSize: Long = 0L,
)

expect fun getMemoryStorageInfo(
    contextWrapper: ContextWrapper,
    db: DatabaseClient,
): MemoryStorageInfo

expect fun clearPlatformCache(contextWrapper: ContextWrapper)
