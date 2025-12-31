/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.localdata.config

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

class DatasourceConfig {

    private val dbName = "appData.db"
    private val migrations = arrayOf(
        Migration1,
        Migration2,
        Migration3,
        Migration4,
        Migration5,
        Migration_6_7,
        Migration_7_8,
        Migration_8_9,
        Migration_9_10,
        Migration_10_11,
        Migration_11_12,
        Migration_12_13,
    )

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
            db.execSQL(
                """
                    CREATE TABLE `xy_download` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT NOT NULL, `fileName` TEXT NOT NULL, `filePath` TEXT NOT NULL, `fileSize` INTEGER NOT NULL, `tempFilePath` TEXT NOT NULL, `typeData` TEXT NOT NULL, `progress` REAL NOT NULL, `totalBytes` INTEGER NOT NULL, `downloadedBytes` INTEGER NOT NULL, `status` TEXT NOT NULL, `error` TEXT, `uid` TEXT, `title` TEXT, `cover` TEXT, `duration` INTEGER, `connectionId` INTEGER, `extend` TEXT, `music` TEXT, `updateTime` INTEGER NOT NULL, `createTime` INTEGER NOT NULL);
            """.trimIndent()
            )
        }
    }

    private object Migration3 : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            db.execSQL("ALTER TABLE xy_music ADD COLUMN downloadUrl TEXT NOT NULL DEFAULT ''")
        }
    }

    private object Migration4 : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            db.execSQL("ALTER TABLE PlayQueueMusic ADD COLUMN picByte BLOB")
        }
    }

    private object Migration5 : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 数据库的升级语句
            db.execSQL("ALTER TABLE xy_artist ADD COLUMN backdrop TEXT")
        }
    }

    private object Migration_6_7 : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            ALTER TABLE xy_background_config 
            ADD COLUMN dailyRecommendBrash TEXT NOT NULL DEFAULT '#FF6C1577/#FFCC6877'
        """.trimIndent()
            )

            db.execSQL(
                """
            ALTER TABLE xy_background_config 
            ADD COLUMN downloadListBrash TEXT NOT NULL DEFAULT '#FF0D9488/#FF0EA5E9'
        """.trimIndent()
            )

            db.execSQL(
                """
            ALTER TABLE xy_background_config 
            ADD COLUMN localMusicBrash TEXT NOT NULL DEFAULT '#FF0A7B88/#FFFFBA6C'
        """.trimIndent()
            )
        }
    }


    private object Migration_7_8 : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 在 xy_settings 表中新增 imageFilePath 字段，默认值为 NULL
            db.execSQL(
                """
            ALTER TABLE xy_settings 
            ADD COLUMN imageFilePath TEXT
            """
            )
        }
    }
    private object Migration_8_9 : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. 创建新表（移除 imageFilePath 字段）
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS xy_settings_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                cacheUpperLimit TEXT NOT NULL,
                ifDesktopLyrics INTEGER NOT NULL,
                doubleSpeed REAL NOT NULL,
                connectionId INTEGER,
                ifEnableAlbumHistory INTEGER NOT NULL,
                ifHandleAudioFocus INTEGER NOT NULL,
                languageType TEXT,
                latestVersionTime INTEGER NOT NULL,
                latestVersion TEXT NOT NULL,
                lasestApkUrl TEXT NOT NULL,
                maxConcurrentDownloads INTEGER NOT NULL
            )
            """.trimIndent()
            )

            // 2. 从旧表拷贝所有保留下来的字段到新表
            db.execSQL(
                """
            INSERT INTO xy_settings_new (
                id, 
                cacheUpperLimit, 
                ifDesktopLyrics, 
                doubleSpeed, 
                connectionId, 
                ifEnableAlbumHistory, 
                ifHandleAudioFocus, 
                languageType, 
                latestVersionTime, 
                latestVersion, 
                lasestApkUrl,
                maxConcurrentDownloads
            )
            SELECT 
                id, 
                cacheUpperLimit, 
                ifDesktopLyrics, 
                doubleSpeed, 
                connectionId, 
                ifEnableAlbumHistory, 
                ifHandleAudioFocus, 
                languageType, 
                latestVersionTime, 
                latestVersion, 
                lasestApkUrl,
                maxConcurrentDownloads
            FROM xy_settings
            """.trimIndent()
            )

            // 3. 删除旧表
            db.execSQL("DROP TABLE xy_settings")

            // 4. 将新表重命名为正式表名
            db.execSQL("ALTER TABLE xy_settings_new RENAME TO xy_settings")
        }
    }


    private object Migration_9_10 : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // navidrome 扩展 Subsonic Token
            db.execSQL(
                "ALTER TABLE xy_connection_config ADD COLUMN navidromeExtendToken TEXT"
            )

            // navidrome 扩展 Subsonic Salt
            db.execSQL(
                "ALTER TABLE xy_connection_config ADD COLUMN navidromeExtendSalt TEXT"
            )

            // 设备唯一标识（machineIdentifier）
            db.execSQL(
                "ALTER TABLE xy_connection_config ADD COLUMN machineIdentifier TEXT"
            )
        }
    }

    private object Migration_10_11 : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS xy_proxy_config (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                enabled INTEGER NOT NULL,
                address TEXT NOT NULL
            )
            """.trimIndent()
            )
        }
    }

    private object Migration_11_12 : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            ALTER TABLE xy_settings
            ADD COLUMN ifEnableSyncPlayProgress INTEGER NOT NULL DEFAULT 1
            """
            )
        }
    }

    private object Migration_12_13 : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            ALTER TABLE xy_settings
            ADD COLUMN fadeDurationMs INTEGER NOT NULL DEFAULT 300
            """.trimIndent()
            )
        }
    }
}