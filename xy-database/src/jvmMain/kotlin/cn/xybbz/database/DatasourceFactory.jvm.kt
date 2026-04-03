package cn.xybbz.database

import androidx.room.Room
import androidx.room.RoomDatabase
import cn.xybbz.platform.ContextWrapper
import java.io.File

actual inline fun <reified T : DatabaseClient> createDatabaseClientBuilder(dbFileName: String,contextWrapper: ContextWrapper): RoomDatabase.Builder<T> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), dbFileName)
    return Room.databaseBuilder<T>(
        name = dbFile.absolutePath,
    )
}