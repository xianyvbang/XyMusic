package cn.xybbz.database

import androidx.room.Room
import androidx.room.RoomDatabase
import cn.xybbz.platform.ContextWrapper

actual inline fun <reified T : DatabaseClient> createDatabaseClientBuilder(dbFileName:String,contextWrapper: ContextWrapper): RoomDatabase.Builder<T> {
    return Room.databaseBuilder(contextWrapper.context, T::class.java, dbFileName)
        .createFromAsset("database/initData.db")
}