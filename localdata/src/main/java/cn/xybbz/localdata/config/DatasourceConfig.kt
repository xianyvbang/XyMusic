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
        Migration_13_14,
        Migration_14_15,
        Migration_15_16,
        Migration_16_17,
        Migration_17_18,
        Migration_18_19,
        Migration_19_20,
        Migration_20_21,
        Migration_21_22,
        Migration_22_23,
        MIGRATION_23_24,
        MIGRATION_24_25,
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
}