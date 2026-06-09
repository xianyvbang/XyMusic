package cn.xybbz.download.database

import cn.xybbz.database.getRoomDatabase
import cn.xybbz.platform.ContextWrapper

internal const val DB_FILE_NAME = "downloadData.db"

/**
 * 创建DownloadDatabaseClient对象
 */
fun getDownloadRoomDatabase(contextWrapper: ContextWrapper): DownloadDatabaseClient {
    return getRoomDatabase(DB_FILE_NAME, contextWrapper)
}