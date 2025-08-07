package cn.xybbz.localdata.config

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

class DatasourceConfig {

    private val dbName = "appData.db"
    private val migrations = arrayOf(Migration10)

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
            // database.execSQL("")
        }
    }

    private object Migration2 : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            db.execSQL("ALTER TABLE settings RENAME TO xy_settings")
        }
    }

    private object Migration3 : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            // database.execSQL("")
            db.execSQL("ALTER TABLE music_user_info RENAME TO xy_user_info")
        }
    }

    private object Migration4 : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            db.execSQL(
                """
                ALTER TABLE xy_album ADD COLUMN bgColor INTEGER
            """.trimIndent()
            )
            db.execSQL(
                """
                ALTER TABLE xy_artist ADD COLUMN bgColor INTEGER
            """.trimIndent()
            )
        }
    }

    private object Migration5 : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            db.execSQL(
                """
                ALTER TABLE progress ADD COLUMN musicName VARCHAR(255) NOT NULL DEFAULT ''
            """.trimIndent()
            )
        }
    }


    private object Migration7 : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            db.execSQL(
                """
                ALTER TABLE xy_connection_config ADD COLUMN deviceId VARCHAR(255) NOT NULL DEFAULT ''
            """.trimIndent()
            )
        }
    }

    private object Migration8 : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            db.execSQL(
                """
                ALTER TABLE xy_connection_config ADD COLUMN serverName VARCHAR(255) NOT NULL DEFAULT ''
            """.trimIndent()
            )
        }
    }

    private object Migration10 : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            db.execSQL(
                """
                ALTER TABLE xy_music ADD COLUMN ifLyric INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
            )
        }
    }
}