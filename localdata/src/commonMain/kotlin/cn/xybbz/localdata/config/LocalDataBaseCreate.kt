package cn.xybbz.localdata.config

import androidx.room.RoomDatabase
import cn.xybbz.database.getRoomDatabase
import cn.xybbz.platform.ContextWrapper

internal const val DB_FILE_NAME = "appData.db"

/**
 * 创建LocalDatabaseClient对象
 */
fun getLocalRoomDatabase(contextWrapper: ContextWrapper): LocalDatabaseClient {
    return getRoomDatabase(getLocalRoomDatabaseBuilder(DB_FILE_NAME, contextWrapper))
}

expect fun getLocalRoomDatabaseBuilder(
    dbFileName: String,
    contextWrapper: ContextWrapper
): RoomDatabase.Builder<LocalDatabaseClient>