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

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL


val Migration_1_2 =  object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
    }
}

val Migration_4_5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE HomeMusic ADD COLUMN runTimeTicks INTEGER NOT NULL DEFAULT 0")
        connection.execSQL(
            """
            UPDATE HomeMusic
            SET runTimeTicks = COALESCE(
                (
                    SELECT mi.runTimeTicks
                    FROM xy_music mi
                    WHERE mi.itemId = HomeMusic.musicId
                    AND mi.connectionId = HomeMusic.connectionId
                    LIMIT 1
                ),
                0
            )
            """.trimIndent()
        )
    }
}
