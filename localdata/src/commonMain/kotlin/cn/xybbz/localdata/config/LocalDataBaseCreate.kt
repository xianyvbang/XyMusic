package cn.xybbz.localdata.config

import androidx.room.RoomDatabase
import cn.xybbz.database.getRoomDatabase

internal const val DB_FILE_NAME = "appData.db"

/**
 * 创建LocalDatabaseClient对象
 */
fun getLocalRoomDatabase(builderFun: (dbFileName:String) -> RoomDatabase.Builder<LocalDatabaseClient>): LocalDatabaseClient{
    return getRoomDatabase(builderFun(DB_FILE_NAME))
}