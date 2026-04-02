package cn.xybbz.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.Executors

actual class DatasourceFactory(val app: Context) {
   actual inline fun <reified T : DatabaseClient> createDatabaseClientBuilder(dbFileName:String): RoomDatabase.Builder<T> {
        return Room.databaseBuilder(app, T::class.java, dbFileName)
            .createFromAsset("database/initData.db")
    }
}