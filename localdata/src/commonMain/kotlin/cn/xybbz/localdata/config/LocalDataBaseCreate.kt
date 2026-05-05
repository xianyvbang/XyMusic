package cn.xybbz.localdata.config

import cn.xybbz.database.createDatabaseClientBuilder
import cn.xybbz.platform.ContextWrapper

internal const val DB_FILE_NAME = "appData.db"

/**
 * 创建LocalDatabaseClient对象
 */
fun getLocalRoomDatabase(contextWrapper: ContextWrapper): LocalDatabaseClient {
    return createDatabaseClientBuilder<LocalDatabaseClient>(DB_FILE_NAME, contextWrapper)
        .addMigrations(Migration_4_5)
        .fallbackToDestructiveMigration(true)
        .build()
}
