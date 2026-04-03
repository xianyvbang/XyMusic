package cn.xybbz.localdata.config

import androidx.room.RoomDatabase
import cn.xybbz.database.DatasourceFactory
import cn.xybbz.database.getRoomDatabase

internal const val DB_FILE_NAME = "appData.db"

/**
 * 创建LocalDatabaseClient对象
 */
fun getLocalRoomDatabase(): LocalDatabaseClient{
    return getRoomDatabase(getLocalRoomDatabaseBuilder(DB_FILE_NAME))
}

expect fun getLocalRoomDatabaseBuilder(dbFileName:String):RoomDatabase.Builder<LocalDatabaseClient>