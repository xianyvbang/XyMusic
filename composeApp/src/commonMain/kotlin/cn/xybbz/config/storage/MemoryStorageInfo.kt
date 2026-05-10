package cn.xybbz.config.storage

import cn.xybbz.platform.ContextWrapper
import cn.xybbz.localdata.config.LocalDatabaseClient

/**
 * 内存管理页面展示的存储占用信息，单位统一为字节。
 */
data class MemoryStorageInfo(
    /** 应用缓存目录占用大小，通常可被用户主动清理。 */
    val cacheSize: Long = 0L,
    /** 应用数据目录占用大小，不包含缓存和数据库等单独展示的部分。 */
    val appDataSize: Long = 0L,
    /** 数据库主文件及其 wal/shm/journal 等附属文件占用大小。 */
    val databaseSize: Long = 0L,
)

/**
 * 获取当前平台的存储占用信息。
 *
 * Android 端会读取应用私有目录，桌面端会读取应用运行目录和 JVM 数据库文件。
 */
expect fun getMemoryStorageInfo(
    contextWrapper: ContextWrapper,
    db: LocalDatabaseClient,
): MemoryStorageInfo

/**
 * 清理当前平台可安全删除的缓存内容。
 */
expect fun clearPlatformCache(contextWrapper: ContextWrapper)
