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

val Migration_5_6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE xy_settings ADD COLUMN cacheFilePath TEXT NOT NULL DEFAULT ''")
    }
}

val Migration_6_7 = object : Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ArtistPopularMusic (
                artistKey TEXT NOT NULL,
                musicId TEXT NOT NULL,
                connectionId INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                cachedAt INTEGER NOT NULL,
                PRIMARY KEY(artistKey, musicId, connectionId)
            )
            """.trimIndent()
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_ArtistPopularMusic_artistKey ON ArtistPopularMusic(artistKey)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_ArtistPopularMusic_musicId ON ArtistPopularMusic(musicId)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_ArtistPopularMusic_connectionId ON ArtistPopularMusic(connectionId)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_ArtistPopularMusic_connectionId_artistKey_cachedAt ON ArtistPopularMusic(connectionId, artistKey, cachedAt)")

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS SimilarMusic (
                sourceMusicId TEXT NOT NULL,
                musicId TEXT NOT NULL,
                connectionId INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                cachedAt INTEGER NOT NULL,
                PRIMARY KEY(sourceMusicId, musicId, connectionId)
            )
            """.trimIndent()
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_SimilarMusic_sourceMusicId ON SimilarMusic(sourceMusicId)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_SimilarMusic_musicId ON SimilarMusic(musicId)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_SimilarMusic_connectionId ON SimilarMusic(connectionId)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_SimilarMusic_connectionId_sourceMusicId_cachedAt ON SimilarMusic(connectionId, sourceMusicId, cachedAt)")
    }
}

val Migration_7_8 = object : Migration(7, 8) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS xy_connection_config_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                serverId TEXT NOT NULL,
                serverName TEXT NOT NULL,
                serverVersion TEXT NOT NULL,
                deviceId TEXT NOT NULL,
                name TEXT NOT NULL,
                address TEXT NOT NULL,
                type TEXT NOT NULL,
                userId TEXT NOT NULL,
                username TEXT NOT NULL,
                currentPassword TEXT NOT NULL,
                iv TEXT NOT NULL,
                key TEXT NOT NULL,
                libraryIds TEXT,
                extendInfo TEXT,
                lastLoginTime INTEGER NOT NULL,
                updateTime INTEGER NOT NULL,
                createTime INTEGER NOT NULL,
                ifEnabledDownload INTEGER NOT NULL,
                ifEnabledDelete INTEGER NOT NULL
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO xy_connection_config_new (
                id,
                serverId,
                serverName,
                serverVersion,
                deviceId,
                name,
                address,
                type,
                userId,
                username,
                currentPassword,
                iv,
                key,
                libraryIds,
                extendInfo,
                lastLoginTime,
                updateTime,
                createTime,
                ifEnabledDownload,
                ifEnabledDelete
            )
            SELECT
                id,
                serverId,
                serverName,
                serverVersion,
                deviceId,
                name,
                address,
                type,
                userId,
                username,
                currentPassword,
                iv,
                key,
                libraryIds,
                extendInfo,
                lastLoginTime,
                updateTime,
                createTime,
                ifEnabledDownload,
                ifEnabledDelete
            FROM xy_connection_config
            """.trimIndent()
        )
        connection.execSQL("DROP TABLE xy_connection_config")
        connection.execSQL("ALTER TABLE xy_connection_config_new RENAME TO xy_connection_config")
    }
}

val Migration_8_9 = object : Migration(8, 9) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS xy_connection_config_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                serverId TEXT NOT NULL,
                serverName TEXT NOT NULL,
                serverVersion TEXT NOT NULL,
                deviceId TEXT NOT NULL,
                name TEXT NOT NULL,
                address TEXT NOT NULL,
                type TEXT NOT NULL,
                userId TEXT NOT NULL,
                username TEXT NOT NULL,
                currentPassword TEXT NOT NULL,
                iv TEXT NOT NULL,
                credentialStoreType TEXT NOT NULL,
                libraryIds TEXT,
                extendInfo TEXT,
                lastLoginTime INTEGER NOT NULL,
                updateTime INTEGER NOT NULL,
                createTime INTEGER NOT NULL,
                ifEnabledDownload INTEGER NOT NULL,
                ifEnabledDelete INTEGER NOT NULL
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO xy_connection_config_new (
                id,
                serverId,
                serverName,
                serverVersion,
                deviceId,
                name,
                address,
                type,
                userId,
                username,
                currentPassword,
                iv,
                credentialStoreType,
                libraryIds,
                extendInfo,
                lastLoginTime,
                updateTime,
                createTime,
                ifEnabledDownload,
                ifEnabledDelete
            )
            SELECT
                id,
                serverId,
                serverName,
                serverVersion,
                deviceId,
                name,
                address,
                type,
                userId,
                username,
                '',
                '',
                'NONE',
                libraryIds,
                extendInfo,
                lastLoginTime,
                updateTime,
                createTime,
                ifEnabledDownload,
                ifEnabledDelete
            FROM xy_connection_config
            """.trimIndent()
        )
        connection.execSQL("DROP TABLE xy_connection_config")
        connection.execSQL("ALTER TABLE xy_connection_config_new RENAME TO xy_connection_config")
        connection.execSQL("ALTER TABLE xy_settings ADD COLUMN ifSyncPasswordsByICloud INTEGER NOT NULL DEFAULT 0")
    }
}
