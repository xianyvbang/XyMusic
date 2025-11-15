package cn.xybbz.localdata.config

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

class DatasourceConfig {

    private val dbName = "appData.db"
    private val migrations = arrayOf(Migration1,Migration2,Migration3)

    fun createDatabaseClient(context: Context): DatabaseClient {
        return Room.databaseBuilder(context.applicationContext, DatabaseClient::class.java, dbName)
            .createFromAsset("database/initData.db")
            .addCallback(CreatedCallBack)
            .setQueryCallback(queryCallback, Executors.newSingleThreadExecutor())
            .addMigrations(*migrations)
            .build()
    }

    private val queryCallback = RoomDatabase.QueryCallback { sqlQuery, bindArgs ->

        /**
         * Called when a SQL query is executed.
         *
         * @param sqlQuery The SQLite query statement.
         * @param bindArgs Arguments of the query if available, empty list otherwise.
         */
//        Log.d("RoomQuery", "SQL Query: $sqlQuery")
//        Log.d("RoomQuery", "Bind Args: $bindArgs")
    }

    private object CreatedCallBack : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            //todo 直接这里执行sql
            /*MIGRATIONS.map {
                it.migrate(db)
            }*/
        }
    }

    private object Migration1 : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            db.execSQL("ALTER TABLE xy_connection_config DROP COLUMN ifEnable")
        }
    }

    private object Migration2 : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            //todo 需要新的语句
            db.execSQL(
                """
                CREATE TABLE `xy_download` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT NOT NULL, `fileName` TEXT NOT NULL, `filePath` TEXT NOT NULL, `fileSize` INTEGER NOT NULL, `tempFilePath` TEXT NOT NULL, `typeData` TEXT NOT NULL, `progress` REAL NOT NULL, `totalBytes` INTEGER NOT NULL, `downloadedBytes` INTEGER NOT NULL, `status` TEXT NOT NULL, `error` TEXT, `uid` TEXT, `title` TEXT, `cover` TEXT, `duration` INTEGER, `connectionId` INTEGER, `updateTime` INTEGER NOT NULL, `createTime` INTEGER NOT NULL);
            """.trimIndent()
            )
        }
    }

    private object Migration3 : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            db.execSQL("ALTER TABLE xy_music ADD COLUMN downloadUrl TEXT NOT NULL")
        }
    }
}