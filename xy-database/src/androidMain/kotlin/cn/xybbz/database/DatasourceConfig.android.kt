package cn.xybbz.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import cn.xybbz.platform.ContextWrapper
import kotlinx.coroutines.Dispatchers

actual inline fun <reified T : DatabaseClient> createDatabaseClientBuilder(dbFileName:String,contextWrapper: ContextWrapper): RoomDatabase.Builder<T> {
    return Room.databaseBuilder(contextWrapper.context, T::class.java, dbFileName)
        .createFromAsset("database/initData.db")
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
}