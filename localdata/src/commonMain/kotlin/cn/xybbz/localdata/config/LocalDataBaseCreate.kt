package cn.xybbz.localdata.config

import cn.xybbz.database.getRoomDatabase
import cn.xybbz.platform.ContextWrapper

internal const val DB_FILE_NAME = "appData.db"

/**
 * 创建LocalDatabaseClient对象
 */
fun getLocalRoomDatabase(contextWrapper: ContextWrapper): LocalDatabaseClient {
    return getRoomDatabase(DB_FILE_NAME, contextWrapper)
}