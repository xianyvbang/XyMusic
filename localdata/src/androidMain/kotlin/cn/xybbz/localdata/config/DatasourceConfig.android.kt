package cn.xybbz.localdata.config

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.Executors

actual class DatasourceFactory(private val app: Context) {
   actual fun createDatabaseClientBuilder(): RoomDatabase.Builder<DatabaseClient> {
        return Room.databaseBuilder(app, DatabaseClient::class.java, DB_FILE_NAME)
            .createFromAsset("database/initData.db")
    }
}