package cn.xybbz.localdata.config

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual class DatasourceFactory {
    actual fun createDatabaseClientBuilder(): RoomDatabase.Builder<DatabaseClient> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "downloads")
        return Room.databaseBuilder<DatabaseClient>(
            name = dbFile.absolutePath,
        )
    }
}