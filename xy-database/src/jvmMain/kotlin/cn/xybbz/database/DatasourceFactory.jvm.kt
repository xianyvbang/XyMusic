package cn.xybbz.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual class DatasourceFactory {
    actual inline fun <reified T : DatabaseClient> createDatabaseClientBuilder(dbFileName: String): RoomDatabase.Builder<T> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), dbFileName)
        return Room.databaseBuilder<T>(
            name = dbFile.absolutePath,
        )
    }
}