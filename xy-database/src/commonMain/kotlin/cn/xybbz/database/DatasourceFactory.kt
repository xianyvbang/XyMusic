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

package cn.xybbz.database

import androidx.room.RoomDatabase
import androidx.room.immediateTransaction
import androidx.room.migration.Migration
import androidx.room.useWriterConnection


expect class DatasourceFactory {
    inline fun <reified T : DatabaseClient> createDatabaseClientBuilder(dbFileName: String): RoomDatabase.Builder<T>
}

fun <T : DatabaseClient> getRoomDatabase(
    builder: RoomDatabase.Builder<T>,
    vararg migrations: Migration
): T {
    return builder.addMigrations(*migrations).build()
}

suspend fun <R> RoomDatabase.withTransaction(block: suspend () -> R): R {
    return this.useWriterConnection { transactor ->
        transactor.immediateTransaction {
            block()
        }
    }
}