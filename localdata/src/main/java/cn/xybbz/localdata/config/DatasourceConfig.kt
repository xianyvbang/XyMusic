package cn.xybbz.localdata.config

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

class DatasourceConfig {

    private val dbName = "appData.db"
    private val migrations = arrayOf(Migration1,Migration2)

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
            db.execSQL("""
                CREATE TABLE "xy_download" (
  "id" INTEGER NOT NULL,
  "url" TEXT,
  "fileName" TEXT,
  "filePath" TEXT,
  "fileSize" integer,
  "tempFilePath" TEXT,
  "progress" integer,
  "totalBytes" integer,
  "downloadedBytes" integer,
  "error" TEXT,
  "uid" text,
  "title" TEXT,
  "cover" TEXT,
  "duration" integer,
  "connectionId" INTEGER,
  "updateTime" integer,
  "createTime" integer,
  PRIMARY KEY ("id")
);
            """.trimIndent())
        }
    }
}