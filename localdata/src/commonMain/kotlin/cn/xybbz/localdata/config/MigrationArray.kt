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

/**
 * v9 到 v10 的外键治理迁移：重建核心表并过滤旧库里已经存在的孤儿关系。
 */
val Migration_9_10 = object : Migration(9, 10) {
    override fun migrate(connection: SQLiteConnection) {
        listOf(
            """
            CREATE TABLE IF NOT EXISTS `xy_connection_config_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `serverId` TEXT NOT NULL,
                `serverName` TEXT NOT NULL,
                `serverVersion` TEXT NOT NULL,
                `deviceId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `address` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `username` TEXT NOT NULL,
                `currentPassword` TEXT NOT NULL,
                `iv` TEXT NOT NULL,
                `credentialStoreType` TEXT NOT NULL,
                `libraryIds` TEXT,
                `extendInfo` TEXT,
                `lastLoginTime` INTEGER NOT NULL,
                `updateTime` INTEGER NOT NULL,
                `createTime` INTEGER NOT NULL,
                `ifEnabledDownload` INTEGER NOT NULL,
                `ifEnabledDelete` INTEGER NOT NULL
            )
            """,
            """
            INSERT INTO `xy_connection_config_new` (
                `id`, `serverId`, `serverName`, `serverVersion`, `deviceId`, `name`,
                `address`, `type`, `userId`, `username`, `currentPassword`, `iv`,
                `credentialStoreType`, `libraryIds`, `extendInfo`, `lastLoginTime`,
                `updateTime`, `createTime`, `ifEnabledDownload`, `ifEnabledDelete`
            )
            SELECT
                `id`, `serverId`, `serverName`, `serverVersion`, `deviceId`, `name`,
                `address`, `type`, `userId`, `username`, `currentPassword`, `iv`,
                `credentialStoreType`, `libraryIds`, `extendInfo`, `lastLoginTime`,
                `updateTime`, `createTime`, `ifEnabledDownload`, `ifEnabledDelete`
            FROM `xy_connection_config`
            """,
            "DROP TABLE `xy_connection_config`",
            "ALTER TABLE `xy_connection_config_new` RENAME TO `xy_connection_config`"
        ).forEach { connection.execSQL(it.trimIndent()) }

        listOf(
            """
            CREATE TABLE IF NOT EXISTS `xy_music_new` (
                `itemId` TEXT NOT NULL,
                `pic` TEXT,
                `name` TEXT NOT NULL,
                `downloadUrl` TEXT NOT NULL,
                `album` TEXT NOT NULL,
                `albumName` TEXT,
                `genreIds` TEXT,
                `connectionId` INTEGER NOT NULL,
                `artists` TEXT,
                `artistIds` TEXT,
                `albumArtist` TEXT,
                `albumArtistIds` TEXT,
                `year` INTEGER,
                `playedCount` INTEGER NOT NULL,
                `ifFavoriteStatus` INTEGER NOT NULL,
                `ifLyric` INTEGER NOT NULL,
                `lyric` TEXT,
                `path` TEXT NOT NULL,
                `bitRate` INTEGER,
                `sampleRate` INTEGER,
                `bitDepth` INTEGER,
                `size` INTEGER,
                `runTimeTicks` INTEGER NOT NULL,
                `container` TEXT,
                `codec` TEXT,
                `playlistItemId` TEXT,
                `plexPlayKey` TEXT,
                `lastPlayedDate` INTEGER NOT NULL,
                `createTime` INTEGER NOT NULL,
                PRIMARY KEY(`itemId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `xy_music_new` (
                `itemId`, `pic`, `name`, `downloadUrl`, `album`, `albumName`, `genreIds`,
                `connectionId`, `artists`, `artistIds`, `albumArtist`, `albumArtistIds`,
                `year`, `playedCount`, `ifFavoriteStatus`, `ifLyric`, `lyric`, `path`,
                `bitRate`, `sampleRate`, `bitDepth`, `size`, `runTimeTicks`, `container`,
                `codec`, `playlistItemId`, `plexPlayKey`, `lastPlayedDate`, `createTime`
            )
            SELECT
                `itemId`, `pic`, `name`, `downloadUrl`, `album`, `albumName`, `genreIds`,
                `connectionId`, `artists`, `artistIds`, `albumArtist`, `albumArtistIds`,
                `year`, `playedCount`, `ifFavoriteStatus`, `ifLyric`, `lyric`, `path`,
                `bitRate`, `sampleRate`, `bitDepth`, `size`, `runTimeTicks`, `container`,
                `codec`, `playlistItemId`, `plexPlayKey`, `lastPlayedDate`, `createTime`
            FROM `xy_music`
            WHERE EXISTS (
                SELECT 1 FROM `xy_connection_config` cc
                WHERE cc.`id` = `xy_music`.`connectionId`
            )
            """,
            "DROP TABLE `xy_music`",
            "ALTER TABLE `xy_music_new` RENAME TO `xy_music`",
            """
            CREATE TABLE IF NOT EXISTS `xy_album_new` (
                `itemId` TEXT NOT NULL,
                `pic` TEXT,
                `name` TEXT NOT NULL,
                `artists` TEXT,
                `artistIds` TEXT,
                `genreIds` TEXT,
                `connectionId` INTEGER NOT NULL,
                `year` INTEGER,
                `premiereDate` INTEGER,
                `ifPlaylist` INTEGER NOT NULL,
                `musicCount` INTEGER NOT NULL,
                `createTime` INTEGER NOT NULL,
                PRIMARY KEY(`itemId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `xy_album_new` (
                `itemId`, `pic`, `name`, `artists`, `artistIds`, `genreIds`,
                `connectionId`, `year`, `premiereDate`, `ifPlaylist`, `musicCount`, `createTime`
            )
            SELECT
                `itemId`, `pic`, `name`, `artists`, `artistIds`, `genreIds`,
                `connectionId`, `year`, `premiereDate`, `ifPlaylist`, `musicCount`, `createTime`
            FROM `xy_album`
            WHERE EXISTS (
                SELECT 1 FROM `xy_connection_config` cc
                WHERE cc.`id` = `xy_album`.`connectionId`
            )
            """,
            "DROP TABLE `xy_album`",
            "ALTER TABLE `xy_album_new` RENAME TO `xy_album`",
            """
            CREATE TABLE IF NOT EXISTS `xy_artist_new` (
                `artistId` TEXT NOT NULL,
                `pic` TEXT,
                `backdrop` TEXT,
                `describe` TEXT,
                `name` TEXT,
                `sortName` TEXT,
                `connectionId` INTEGER NOT NULL,
                `musicCount` INTEGER,
                `albumCount` INTEGER,
                `selectChat` TEXT NOT NULL,
                PRIMARY KEY(`artistId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `xy_artist_new` (
                `artistId`, `pic`, `backdrop`, `describe`, `name`, `sortName`,
                `connectionId`, `musicCount`, `albumCount`, `selectChat`
            )
            SELECT
                `artistId`, `pic`, `backdrop`, `describe`, `name`, `sortName`,
                `connectionId`, `musicCount`, `albumCount`, `selectChat`
            FROM `xy_artist`
            WHERE EXISTS (
                SELECT 1 FROM `xy_connection_config` cc
                WHERE cc.`id` = `xy_artist`.`connectionId`
            )
            """,
            "DROP TABLE `xy_artist`",
            "ALTER TABLE `xy_artist_new` RENAME TO `xy_artist`",
            """
            CREATE TABLE IF NOT EXISTS `xy_genre_new` (
                `itemId` TEXT NOT NULL,
                `pic` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `createTime` INTEGER NOT NULL,
                PRIMARY KEY(`itemId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `xy_genre_new` (`itemId`, `pic`, `name`, `connectionId`, `createTime`)
            SELECT `itemId`, `pic`, `name`, `connectionId`, `createTime`
            FROM `xy_genre`
            WHERE EXISTS (
                SELECT 1 FROM `xy_connection_config` cc
                WHERE cc.`id` = `xy_genre`.`connectionId`
            )
            """,
            "DROP TABLE `xy_genre`",
            "ALTER TABLE `xy_genre_new` RENAME TO `xy_genre`",
            """
            CREATE TABLE IF NOT EXISTS `xy_library_new` (
                `id` TEXT NOT NULL,
                `collectionType` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                PRIMARY KEY(`id`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `xy_library_new` (`id`, `collectionType`, `connectionId`, `name`)
            SELECT `id`, `collectionType`, `connectionId`, `name`
            FROM `xy_library`
            WHERE EXISTS (
                SELECT 1 FROM `xy_connection_config` cc
                WHERE cc.`id` = `xy_library`.`connectionId`
            )
            """,
            "DROP TABLE `xy_library`",
            "ALTER TABLE `xy_library_new` RENAME TO `xy_library`"
        ).forEach { connection.execSQL(it.trimIndent()) }

        listOf(
            """
            CREATE TABLE IF NOT EXISTS `xy_settings_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `ifEnableEdgeDownload` INTEGER NOT NULL,
                `cacheUpperLimit` TEXT NOT NULL,
                `ifDesktopLyrics` INTEGER NOT NULL,
                `doubleSpeed` REAL NOT NULL,
                `connectionId` INTEGER,
                `dataSourceType` TEXT,
                `ifEnableAlbumHistory` INTEGER NOT NULL,
                `ifHandleAudioFocus` INTEGER NOT NULL,
                `languageType` TEXT,
                `latestVersionTime` INTEGER NOT NULL,
                `latestVersion` TEXT NOT NULL,
                `lasestApkUrl` TEXT NOT NULL,
                `maxConcurrentDownloads` INTEGER NOT NULL,
                `ifEnableSyncPlayProgress` INTEGER NOT NULL,
                `fadeDurationMs` INTEGER NOT NULL,
                `ifTranscoding` INTEGER NOT NULL,
                `transcodeFormat` TEXT NOT NULL,
                `mobileNetworkAudioBitRate` INTEGER NOT NULL,
                `wifiNetworkAudioBitRate` INTEGER NOT NULL,
                `ifPriorityMusicApi` INTEGER NOT NULL,
                `customLrcSingleApi` TEXT NOT NULL,
                `customLrcApiAuth` TEXT NOT NULL,
                `customCoverApi` TEXT NOT NULL,
                `playSessionId` TEXT NOT NULL,
                `themeType` TEXT NOT NULL,
                `imageFilePath` TEXT,
                `jvmVolume` INTEGER,
                `cacheFilePath` TEXT NOT NULL,
                `ifSyncPasswordsByICloud` INTEGER NOT NULL,
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE SET NULL
            )
            """,
            """
            INSERT INTO `xy_settings_new` (
                `id`, `ifEnableEdgeDownload`, `cacheUpperLimit`, `ifDesktopLyrics`, `doubleSpeed`,
                `connectionId`, `dataSourceType`, `ifEnableAlbumHistory`, `ifHandleAudioFocus`,
                `languageType`, `latestVersionTime`, `latestVersion`, `lasestApkUrl`,
                `maxConcurrentDownloads`, `ifEnableSyncPlayProgress`, `fadeDurationMs`,
                `ifTranscoding`, `transcodeFormat`, `mobileNetworkAudioBitRate`,
                `wifiNetworkAudioBitRate`, `ifPriorityMusicApi`, `customLrcSingleApi`,
                `customLrcApiAuth`, `customCoverApi`, `playSessionId`, `themeType`,
                `imageFilePath`, `jvmVolume`, `cacheFilePath`, `ifSyncPasswordsByICloud`
            )
            SELECT
                `id`, `ifEnableEdgeDownload`, `cacheUpperLimit`, `ifDesktopLyrics`, `doubleSpeed`,
                CASE
                    WHEN `connectionId` IS NOT NULL
                    AND EXISTS (SELECT 1 FROM `xy_connection_config` cc WHERE cc.`id` = `xy_settings`.`connectionId`)
                    THEN `connectionId`
                    ELSE NULL
                END,
                `dataSourceType`, `ifEnableAlbumHistory`, `ifHandleAudioFocus`,
                `languageType`, `latestVersionTime`, `latestVersion`, `lasestApkUrl`,
                `maxConcurrentDownloads`, `ifEnableSyncPlayProgress`, `fadeDurationMs`,
                `ifTranscoding`, `transcodeFormat`, `mobileNetworkAudioBitRate`,
                `wifiNetworkAudioBitRate`, `ifPriorityMusicApi`, `customLrcSingleApi`,
                `customLrcApiAuth`, `customCoverApi`, `playSessionId`, `themeType`,
                `imageFilePath`, `jvmVolume`, `cacheFilePath`, `ifSyncPasswordsByICloud`
            FROM `xy_settings`
            """,
            "DROP TABLE `xy_settings`",
            "ALTER TABLE `xy_settings_new` RENAME TO `xy_settings`",
            """
            CREATE TABLE IF NOT EXISTS `remote_current_new` (
                `id` TEXT NOT NULL,
                `nextKey` INTEGER NOT NULL,
                `prevKey` INTEGER NOT NULL,
                `total` INTEGER NOT NULL,
                `refresh` INTEGER NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `createTime` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `remote_current_new` (`id`, `nextKey`, `prevKey`, `total`, `refresh`, `connectionId`, `createTime`)
            SELECT `id`, `nextKey`, `prevKey`, `total`, `refresh`, `connectionId`, `createTime`
            FROM `remote_current`
            WHERE EXISTS (
                SELECT 1 FROM `xy_connection_config` cc
                WHERE cc.`id` = `remote_current`.`connectionId`
            )
            """,
            "DROP TABLE `remote_current`",
            "ALTER TABLE `remote_current_new` RENAME TO `remote_current`",
            """
            CREATE TABLE IF NOT EXISTS `search_history_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `searchQuery` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `createTime` INTEGER NOT NULL,
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `search_history_new` (`id`, `searchQuery`, `connectionId`, `createTime`)
            SELECT `id`, `searchQuery`, `connectionId`, `createTime`
            FROM `search_history`
            WHERE EXISTS (
                SELECT 1 FROM `xy_connection_config` cc
                WHERE cc.`id` = `search_history`.`connectionId`
            )
            """,
            "DROP TABLE `search_history`",
            "ALTER TABLE `search_history_new` RENAME TO `search_history`",
            """
            CREATE TABLE IF NOT EXISTS `xy_data_count_new` (
                `connectionId` INTEGER NOT NULL,
                `musicCount` INTEGER,
                `albumCount` INTEGER,
                `artistCount` INTEGER,
                `playlistCount` INTEGER,
                `genreCount` INTEGER,
                `favoriteCount` INTEGER,
                `createTime` INTEGER NOT NULL,
                PRIMARY KEY(`connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `xy_data_count_new` (
                `connectionId`, `musicCount`, `albumCount`, `artistCount`,
                `playlistCount`, `genreCount`, `favoriteCount`, `createTime`
            )
            SELECT
                `connectionId`, `musicCount`, `albumCount`, `artistCount`,
                `playlistCount`, `genreCount`, `favoriteCount`, `createTime`
            FROM `xy_data_count`
            WHERE EXISTS (
                SELECT 1 FROM `xy_connection_config` cc
                WHERE cc.`id` = `xy_data_count`.`connectionId`
            )
            """,
            "DROP TABLE `xy_data_count`",
            "ALTER TABLE `xy_data_count_new` RENAME TO `xy_data_count`",
            """
            CREATE TABLE IF NOT EXISTS `xy_player_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `connectionId` INTEGER,
                `dataType` TEXT NOT NULL,
                `musicId` TEXT NOT NULL,
                `headTime` INTEGER NOT NULL,
                `endTime` INTEGER NOT NULL,
                `playerType` TEXT NOT NULL,
                `pageNum` INTEGER NOT NULL,
                `pageSize` INTEGER NOT NULL,
                `ifSkip` INTEGER NOT NULL,
                `albumId` TEXT NOT NULL,
                `artistId` TEXT,
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `xy_player_new` (
                `id`, `connectionId`, `dataType`, `musicId`, `headTime`, `endTime`,
                `playerType`, `pageNum`, `pageSize`, `ifSkip`, `albumId`, `artistId`
            )
            SELECT
                `id`, `connectionId`, `dataType`, `musicId`, `headTime`, `endTime`,
                `playerType`, `pageNum`, `pageSize`, `ifSkip`, `albumId`, `artistId`
            FROM `xy_player`
            WHERE `connectionId` IS NULL
               OR EXISTS (
                    SELECT 1 FROM `xy_connection_config` cc
                    WHERE cc.`id` = `xy_player`.`connectionId`
               )
            """,
            "DROP TABLE `xy_player`",
            "ALTER TABLE `xy_player_new` RENAME TO `xy_player`"
        ).forEach { connection.execSQL(it.trimIndent()) }

        listOf(
            """
            CREATE TABLE IF NOT EXISTS `HomeMusic_new` (
                `musicId` TEXT NOT NULL,
                `pic` TEXT,
                `name` TEXT NOT NULL,
                `artists` TEXT,
                `album` TEXT NOT NULL,
                `albumName` TEXT,
                `codec` TEXT,
                `bitRate` INTEGER,
                `runTimeTicks` INTEGER NOT NULL DEFAULT 0,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`musicId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`musicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `HomeMusic_new` (
                `musicId`, `pic`, `name`, `artists`, `album`, `albumName`, `codec`,
                `bitRate`, `runTimeTicks`, `connectionId`, `index`, `cachedAt`
            )
            SELECT
                `musicId`, `pic`, `name`, `artists`, `album`, `albumName`, `codec`,
                `bitRate`, `runTimeTicks`, `connectionId`, `index`, `cachedAt`
            FROM `HomeMusic`
            WHERE EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `HomeMusic`.`musicId`
                AND mi.`connectionId` = `HomeMusic`.`connectionId`
            )
            """,
            "DROP TABLE `HomeMusic`",
            "ALTER TABLE `HomeMusic_new` RENAME TO `HomeMusic`",
            """
            CREATE TABLE IF NOT EXISTS `FavoriteMusic_new` (
                `musicId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `ifFavorite` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`musicId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`musicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `FavoriteMusic_new` (`musicId`, `connectionId`, `ifFavorite`, `index`, `cachedAt`)
            SELECT `musicId`, `connectionId`, `ifFavorite`, `index`, `cachedAt`
            FROM `FavoriteMusic`
            WHERE EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `FavoriteMusic`.`musicId`
                AND mi.`connectionId` = `FavoriteMusic`.`connectionId`
            )
            """,
            "DROP TABLE `FavoriteMusic`",
            "ALTER TABLE `FavoriteMusic_new` RENAME TO `FavoriteMusic`",
            """
            CREATE TABLE IF NOT EXISTS `AlbumMusic_new` (
                `albumId` TEXT NOT NULL,
                `musicId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`albumId`, `musicId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`albumId`, `connectionId`) REFERENCES `xy_album`(`itemId`, `connectionId`) ON DELETE CASCADE,
                FOREIGN KEY(`musicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `AlbumMusic_new` (`albumId`, `musicId`, `connectionId`, `index`, `cachedAt`)
            SELECT `albumId`, `musicId`, `connectionId`, `index`, `cachedAt`
            FROM `AlbumMusic`
            WHERE EXISTS (
                SELECT 1 FROM `xy_album` xa
                WHERE xa.`itemId` = `AlbumMusic`.`albumId`
                AND xa.`connectionId` = `AlbumMusic`.`connectionId`
            )
            AND EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `AlbumMusic`.`musicId`
                AND mi.`connectionId` = `AlbumMusic`.`connectionId`
            )
            """,
            "DROP TABLE `AlbumMusic`",
            "ALTER TABLE `AlbumMusic_new` RENAME TO `AlbumMusic`",
            """
            CREATE TABLE IF NOT EXISTS `ArtistMusic_new` (
                `artistId` TEXT NOT NULL,
                `musicId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`artistId`, `musicId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`artistId`, `connectionId`) REFERENCES `xy_artist`(`artistId`, `connectionId`) ON DELETE CASCADE,
                FOREIGN KEY(`musicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `ArtistMusic_new` (`artistId`, `musicId`, `connectionId`, `index`, `cachedAt`)
            SELECT `artistId`, `musicId`, `connectionId`, `index`, `cachedAt`
            FROM `ArtistMusic`
            WHERE EXISTS (
                SELECT 1 FROM `xy_artist` xa
                WHERE xa.`artistId` = `ArtistMusic`.`artistId`
                AND xa.`connectionId` = `ArtistMusic`.`connectionId`
            )
            AND EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `ArtistMusic`.`musicId`
                AND mi.`connectionId` = `ArtistMusic`.`connectionId`
            )
            """,
            "DROP TABLE `ArtistMusic`",
            "ALTER TABLE `ArtistMusic_new` RENAME TO `ArtistMusic`",
            """
            CREATE TABLE IF NOT EXISTS `PlaylistMusic_new` (
                `playlistId` TEXT NOT NULL,
                `musicId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`playlistId`, `musicId`, `connectionId`, `index`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`playlistId`, `connectionId`) REFERENCES `xy_album`(`itemId`, `connectionId`) ON DELETE CASCADE,
                FOREIGN KEY(`musicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `PlaylistMusic_new` (`playlistId`, `musicId`, `connectionId`, `index`, `cachedAt`)
            SELECT `playlistId`, `musicId`, `connectionId`, `index`, `cachedAt`
            FROM `PlaylistMusic`
            WHERE EXISTS (
                SELECT 1 FROM `xy_album` xa
                WHERE xa.`itemId` = `PlaylistMusic`.`playlistId`
                AND xa.`connectionId` = `PlaylistMusic`.`connectionId`
            )
            AND EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `PlaylistMusic`.`musicId`
                AND mi.`connectionId` = `PlaylistMusic`.`connectionId`
            )
            """,
            "DROP TABLE `PlaylistMusic`",
            "ALTER TABLE `PlaylistMusic_new` RENAME TO `PlaylistMusic`"
        ).forEach { connection.execSQL(it.trimIndent()) }

        listOf(
            """
            CREATE TABLE IF NOT EXISTS `PlayHistoryMusic_new` (
                `musicId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`musicId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`musicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `PlayHistoryMusic_new` (`musicId`, `connectionId`, `index`, `cachedAt`)
            SELECT `musicId`, `connectionId`, `index`, `cachedAt`
            FROM `PlayHistoryMusic`
            WHERE EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `PlayHistoryMusic`.`musicId`
                AND mi.`connectionId` = `PlayHistoryMusic`.`connectionId`
            )
            """,
            "DROP TABLE `PlayHistoryMusic`",
            "ALTER TABLE `PlayHistoryMusic_new` RENAME TO `PlayHistoryMusic`",
            """
            CREATE TABLE IF NOT EXISTS `PlayQueueMusic_new` (
                `musicId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`musicId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`musicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `PlayQueueMusic_new` (`musicId`, `connectionId`, `index`, `cachedAt`)
            SELECT `musicId`, `connectionId`, `index`, `cachedAt`
            FROM `PlayQueueMusic`
            WHERE EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `PlayQueueMusic`.`musicId`
                AND mi.`connectionId` = `PlayQueueMusic`.`connectionId`
            )
            """,
            "DROP TABLE `PlayQueueMusic`",
            "ALTER TABLE `PlayQueueMusic_new` RENAME TO `PlayQueueMusic`",
            """
            CREATE TABLE IF NOT EXISTS `MaximumPlayMusic_new` (
                `musicId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`musicId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`musicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `MaximumPlayMusic_new` (`musicId`, `connectionId`, `index`, `cachedAt`)
            SELECT `musicId`, `connectionId`, `index`, `cachedAt`
            FROM `MaximumPlayMusic`
            WHERE EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `MaximumPlayMusic`.`musicId`
                AND mi.`connectionId` = `MaximumPlayMusic`.`connectionId`
            )
            """,
            "DROP TABLE `MaximumPlayMusic`",
            "ALTER TABLE `MaximumPlayMusic_new` RENAME TO `MaximumPlayMusic`",
            """
            CREATE TABLE IF NOT EXISTS `NewestMusic_new` (
                `musicId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`musicId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`musicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `NewestMusic_new` (`musicId`, `connectionId`, `index`, `cachedAt`)
            SELECT `musicId`, `connectionId`, `index`, `cachedAt`
            FROM `NewestMusic`
            WHERE EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `NewestMusic`.`musicId`
                AND mi.`connectionId` = `NewestMusic`.`connectionId`
            )
            """,
            "DROP TABLE `NewestMusic`",
            "ALTER TABLE `NewestMusic_new` RENAME TO `NewestMusic`"
        ).forEach { connection.execSQL(it.trimIndent()) }

        listOf(
            """
            CREATE TABLE IF NOT EXISTS `HomeAlbum_new` (
                `albumId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`albumId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`albumId`, `connectionId`) REFERENCES `xy_album`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `HomeAlbum_new` (`albumId`, `connectionId`, `index`, `cachedAt`)
            SELECT `albumId`, `connectionId`, `index`, `cachedAt`
            FROM `HomeAlbum`
            WHERE EXISTS (
                SELECT 1 FROM `xy_album` xa
                WHERE xa.`itemId` = `HomeAlbum`.`albumId`
                AND xa.`connectionId` = `HomeAlbum`.`connectionId`
            )
            """,
            "DROP TABLE `HomeAlbum`",
            "ALTER TABLE `HomeAlbum_new` RENAME TO `HomeAlbum`",
            """
            CREATE TABLE IF NOT EXISTS `NewestAlbum_new` (
                `albumId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`albumId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`albumId`, `connectionId`) REFERENCES `xy_album`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `NewestAlbum_new` (`albumId`, `connectionId`, `index`, `cachedAt`)
            SELECT `albumId`, `connectionId`, `index`, `cachedAt`
            FROM `NewestAlbum`
            WHERE EXISTS (
                SELECT 1 FROM `xy_album` xa
                WHERE xa.`itemId` = `NewestAlbum`.`albumId`
                AND xa.`connectionId` = `NewestAlbum`.`connectionId`
            )
            """,
            "DROP TABLE `NewestAlbum`",
            "ALTER TABLE `NewestAlbum_new` RENAME TO `NewestAlbum`",
            """
            CREATE TABLE IF NOT EXISTS `PlayHistoryAlbum_new` (
                `albumId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`albumId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`albumId`, `connectionId`) REFERENCES `xy_album`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `PlayHistoryAlbum_new` (`albumId`, `connectionId`, `index`, `cachedAt`)
            SELECT `albumId`, `connectionId`, `index`, `cachedAt`
            FROM `PlayHistoryAlbum`
            WHERE EXISTS (
                SELECT 1 FROM `xy_album` xa
                WHERE xa.`itemId` = `PlayHistoryAlbum`.`albumId`
                AND xa.`connectionId` = `PlayHistoryAlbum`.`connectionId`
            )
            """,
            "DROP TABLE `PlayHistoryAlbum`",
            "ALTER TABLE `PlayHistoryAlbum_new` RENAME TO `PlayHistoryAlbum`",
            """
            CREATE TABLE IF NOT EXISTS `MaximumPlayAlbum_new` (
                `albumId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`albumId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`albumId`, `connectionId`) REFERENCES `xy_album`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `MaximumPlayAlbum_new` (`albumId`, `connectionId`, `index`, `cachedAt`)
            SELECT `albumId`, `connectionId`, `index`, `cachedAt`
            FROM `MaximumPlayAlbum`
            WHERE EXISTS (
                SELECT 1 FROM `xy_album` xa
                WHERE xa.`itemId` = `MaximumPlayAlbum`.`albumId`
                AND xa.`connectionId` = `MaximumPlayAlbum`.`connectionId`
            )
            """,
            "DROP TABLE `MaximumPlayAlbum`",
            "ALTER TABLE `MaximumPlayAlbum_new` RENAME TO `MaximumPlayAlbum`",
            """
            CREATE TABLE IF NOT EXISTS `FavoriteAlbum_new` (
                `albumId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `ifFavorite` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`albumId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`albumId`, `connectionId`) REFERENCES `xy_album`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `FavoriteAlbum_new` (`albumId`, `connectionId`, `ifFavorite`, `cachedAt`)
            SELECT `albumId`, `connectionId`, `ifFavorite`, `cachedAt`
            FROM `FavoriteAlbum`
            WHERE EXISTS (
                SELECT 1 FROM `xy_album` xa
                WHERE xa.`itemId` = `FavoriteAlbum`.`albumId`
                AND xa.`connectionId` = `FavoriteAlbum`.`connectionId`
            )
            """,
            "DROP TABLE `FavoriteAlbum`",
            "ALTER TABLE `FavoriteAlbum_new` RENAME TO `FavoriteAlbum`"
        ).forEach { connection.execSQL(it.trimIndent()) }

        listOf(
            """
            CREATE TABLE IF NOT EXISTS `ArtistAlbum_new` (
                `artistId` TEXT NOT NULL,
                `albumId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`artistId`, `albumId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`artistId`, `connectionId`) REFERENCES `xy_artist`(`artistId`, `connectionId`) ON DELETE CASCADE,
                FOREIGN KEY(`albumId`, `connectionId`) REFERENCES `xy_album`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `ArtistAlbum_new` (`artistId`, `albumId`, `connectionId`, `index`, `cachedAt`)
            SELECT `artistId`, `albumId`, `connectionId`, `index`, `cachedAt`
            FROM `ArtistAlbum`
            WHERE EXISTS (
                SELECT 1 FROM `xy_artist` xa
                WHERE xa.`artistId` = `ArtistAlbum`.`artistId`
                AND xa.`connectionId` = `ArtistAlbum`.`connectionId`
            )
            AND EXISTS (
                SELECT 1 FROM `xy_album` al
                WHERE al.`itemId` = `ArtistAlbum`.`albumId`
                AND al.`connectionId` = `ArtistAlbum`.`connectionId`
            )
            """,
            "DROP TABLE `ArtistAlbum`",
            "ALTER TABLE `ArtistAlbum_new` RENAME TO `ArtistAlbum`",
            """
            CREATE TABLE IF NOT EXISTS `GenreAlbum_new` (
                `genreId` TEXT NOT NULL,
                `albumId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`albumId`, `genreId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`albumId`, `connectionId`) REFERENCES `xy_album`(`itemId`, `connectionId`) ON DELETE CASCADE,
                FOREIGN KEY(`genreId`, `connectionId`) REFERENCES `xy_genre`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `GenreAlbum_new` (`genreId`, `albumId`, `connectionId`, `index`, `cachedAt`)
            SELECT `genreId`, `albumId`, `connectionId`, `index`, `cachedAt`
            FROM `GenreAlbum`
            WHERE EXISTS (
                SELECT 1 FROM `xy_album` al
                WHERE al.`itemId` = `GenreAlbum`.`albumId`
                AND al.`connectionId` = `GenreAlbum`.`connectionId`
            )
            AND EXISTS (
                SELECT 1 FROM `xy_genre` ge
                WHERE ge.`itemId` = `GenreAlbum`.`genreId`
                AND ge.`connectionId` = `GenreAlbum`.`connectionId`
            )
            """,
            "DROP TABLE `GenreAlbum`",
            "ALTER TABLE `GenreAlbum_new` RENAME TO `GenreAlbum`",
            """
            CREATE TABLE IF NOT EXISTS `FavoriteArtist_new` (
                `artistId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `ifFavorite` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`artistId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`artistId`, `connectionId`) REFERENCES `xy_artist`(`artistId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `FavoriteArtist_new` (`artistId`, `connectionId`, `ifFavorite`, `cachedAt`)
            SELECT `artistId`, `connectionId`, `ifFavorite`, `cachedAt`
            FROM `FavoriteArtist`
            WHERE EXISTS (
                SELECT 1 FROM `xy_artist` xa
                WHERE xa.`artistId` = `FavoriteArtist`.`artistId`
                AND xa.`connectionId` = `FavoriteArtist`.`connectionId`
            )
            """,
            "DROP TABLE `FavoriteArtist`",
            "ALTER TABLE `FavoriteArtist_new` RENAME TO `FavoriteArtist`"
        ).forEach { connection.execSQL(it.trimIndent()) }

        listOf(
            """
            CREATE TABLE IF NOT EXISTS `progress_new` (
                `musicId` TEXT NOT NULL,
                `musicName` TEXT NOT NULL,
                `albumId` TEXT NOT NULL,
                `progress` INTEGER NOT NULL,
                `progressPercentage` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `createTime` INTEGER NOT NULL,
                PRIMARY KEY(`musicId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`musicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `progress_new` (
                `musicId`, `musicName`, `albumId`, `progress`, `progressPercentage`,
                `index`, `connectionId`, `createTime`
            )
            SELECT
                `musicId`, `musicName`, `albumId`, `progress`, `progressPercentage`,
                `index`, `connectionId`, `createTime`
            FROM `progress`
            WHERE EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `progress`.`musicId`
                AND mi.`connectionId` = `progress`.`connectionId`
            )
            """,
            "DROP TABLE `progress`",
            "ALTER TABLE `progress_new` RENAME TO `progress`",
            """
            CREATE TABLE IF NOT EXISTS `xy_enable_progress_new` (
                `albumId` TEXT NOT NULL,
                `ifEnableAlbumHistory` INTEGER NOT NULL,
                `connectionId` INTEGER NOT NULL,
                PRIMARY KEY(`albumId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`albumId`, `connectionId`) REFERENCES `xy_album`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `xy_enable_progress_new` (`albumId`, `ifEnableAlbumHistory`, `connectionId`)
            SELECT `albumId`, `ifEnableAlbumHistory`, `connectionId`
            FROM `xy_enable_progress`
            WHERE EXISTS (
                SELECT 1 FROM `xy_album` xa
                WHERE xa.`itemId` = `xy_enable_progress`.`albumId`
                AND xa.`connectionId` = `xy_enable_progress`.`connectionId`
            )
            """,
            "DROP TABLE `xy_enable_progress`",
            "ALTER TABLE `xy_enable_progress_new` RENAME TO `xy_enable_progress`",
            """
            CREATE TABLE IF NOT EXISTS `skip_time_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `albumId` TEXT NOT NULL,
                `headTime` INTEGER NOT NULL,
                `endTime` INTEGER NOT NULL,
                `connectionId` INTEGER NOT NULL,
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`albumId`, `connectionId`) REFERENCES `xy_album`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `skip_time_new` (`id`, `albumId`, `headTime`, `endTime`, `connectionId`)
            SELECT `id`, `albumId`, `headTime`, `endTime`, `connectionId`
            FROM `skip_time`
            WHERE EXISTS (
                SELECT 1 FROM `xy_album` xa
                WHERE xa.`itemId` = `skip_time`.`albumId`
                AND xa.`connectionId` = `skip_time`.`connectionId`
            )
            """,
            "DROP TABLE `skip_time`",
            "ALTER TABLE `skip_time_new` RENAME TO `skip_time`",
            """
            CREATE TABLE IF NOT EXISTS `xy_lrc_config_new` (
                `id` INTEGER NOT NULL,
                `itemId` TEXT NOT NULL,
                `lrcOffsetMs` INTEGER NOT NULL,
                `connectionId` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`itemId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `xy_lrc_config_new` (`id`, `itemId`, `lrcOffsetMs`, `connectionId`)
            SELECT `id`, `itemId`, `lrcOffsetMs`, `connectionId`
            FROM `xy_lrc_config`
            WHERE EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `xy_lrc_config`.`itemId`
                AND mi.`connectionId` = `xy_lrc_config`.`connectionId`
            )
            """,
            "DROP TABLE `xy_lrc_config`",
            "ALTER TABLE `xy_lrc_config_new` RENAME TO `xy_lrc_config`"
        ).forEach { connection.execSQL(it.trimIndent()) }

        listOf(
            """
            CREATE TABLE IF NOT EXISTS `xy_daily_recommend_history_new` (
                `songId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `mediaLibraryId` TEXT,
                `recommendIndex` INTEGER NOT NULL,
                `timestamp` INTEGER NOT NULL,
                PRIMARY KEY(`songId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`songId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `xy_daily_recommend_history_new` (
                `songId`, `connectionId`, `mediaLibraryId`, `recommendIndex`, `timestamp`
            )
            SELECT `songId`, `connectionId`, `mediaLibraryId`, `recommendIndex`, `timestamp`
            FROM `xy_daily_recommend_history`
            WHERE EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `xy_daily_recommend_history`.`songId`
                AND mi.`connectionId` = `xy_daily_recommend_history`.`connectionId`
            )
            """,
            "DROP TABLE `xy_daily_recommend_history`",
            "ALTER TABLE `xy_daily_recommend_history_new` RENAME TO `xy_daily_recommend_history`",
            """
            CREATE TABLE IF NOT EXISTS `ArtistPopularMusic_new` (
                `artistKey` TEXT NOT NULL,
                `musicId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`artistKey`, `musicId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`musicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `ArtistPopularMusic_new` (`artistKey`, `musicId`, `connectionId`, `index`, `cachedAt`)
            SELECT `artistKey`, `musicId`, `connectionId`, `index`, `cachedAt`
            FROM `ArtistPopularMusic`
            WHERE EXISTS (
                SELECT 1 FROM `xy_music` mi
                WHERE mi.`itemId` = `ArtistPopularMusic`.`musicId`
                AND mi.`connectionId` = `ArtistPopularMusic`.`connectionId`
            )
            """,
            "DROP TABLE `ArtistPopularMusic`",
            "ALTER TABLE `ArtistPopularMusic_new` RENAME TO `ArtistPopularMusic`",
            """
            CREATE TABLE IF NOT EXISTS `SimilarMusic_new` (
                `sourceMusicId` TEXT NOT NULL,
                `musicId` TEXT NOT NULL,
                `connectionId` INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`sourceMusicId`, `musicId`, `connectionId`),
                FOREIGN KEY(`connectionId`) REFERENCES `xy_connection_config`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`sourceMusicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE,
                FOREIGN KEY(`musicId`, `connectionId`) REFERENCES `xy_music`(`itemId`, `connectionId`) ON DELETE CASCADE
            )
            """,
            """
            INSERT INTO `SimilarMusic_new` (`sourceMusicId`, `musicId`, `connectionId`, `index`, `cachedAt`)
            SELECT `sourceMusicId`, `musicId`, `connectionId`, `index`, `cachedAt`
            FROM `SimilarMusic`
            WHERE EXISTS (
                SELECT 1 FROM `xy_music` source
                WHERE source.`itemId` = `SimilarMusic`.`sourceMusicId`
                AND source.`connectionId` = `SimilarMusic`.`connectionId`
            )
            AND EXISTS (
                SELECT 1 FROM `xy_music` target
                WHERE target.`itemId` = `SimilarMusic`.`musicId`
                AND target.`connectionId` = `SimilarMusic`.`connectionId`
            )
            """,
            "DROP TABLE `SimilarMusic`",
            "ALTER TABLE `SimilarMusic_new` RENAME TO `SimilarMusic`"
        ).forEach { connection.execSQL(it.trimIndent()) }

        listOf(
            "CREATE INDEX IF NOT EXISTS `index_xy_music_connectionId` ON `xy_music` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_album_connectionId` ON `xy_album` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_artist_connectionId` ON `xy_artist` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_genre_connectionId` ON `xy_genre` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_library_connectionId` ON `xy_library` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_settings_connectionId` ON `xy_settings` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_remote_current_connectionId` ON `remote_current` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_search_history_connectionId` ON `search_history` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_data_count_connectionId` ON `xy_data_count` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_player_connectionId` ON `xy_player` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_HomeMusic_connectionId` ON `HomeMusic` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_HomeMusic_musicId` ON `HomeMusic` (`musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_HomeMusic_musicId_connectionId` ON `HomeMusic` (`musicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_FavoriteMusic_musicId` ON `FavoriteMusic` (`musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_FavoriteMusic_connectionId` ON `FavoriteMusic` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_FavoriteMusic_musicId_connectionId` ON `FavoriteMusic` (`musicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_AlbumMusic_albumId` ON `AlbumMusic` (`albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_AlbumMusic_connectionId` ON `AlbumMusic` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_AlbumMusic_musicId` ON `AlbumMusic` (`musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_AlbumMusic_albumId_connectionId` ON `AlbumMusic` (`albumId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_AlbumMusic_musicId_connectionId` ON `AlbumMusic` (`musicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistMusic_musicId` ON `ArtistMusic` (`musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistMusic_connectionId` ON `ArtistMusic` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistMusic_artistId` ON `ArtistMusic` (`artistId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistMusic_artistId_connectionId` ON `ArtistMusic` (`artistId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistMusic_musicId_connectionId` ON `ArtistMusic` (`musicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlaylistMusic_musicId` ON `PlaylistMusic` (`musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlaylistMusic_connectionId` ON `PlaylistMusic` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlaylistMusic_playlistId` ON `PlaylistMusic` (`playlistId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlaylistMusic_playlistId_connectionId` ON `PlaylistMusic` (`playlistId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlaylistMusic_musicId_connectionId` ON `PlaylistMusic` (`musicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlayHistoryMusic_musicId` ON `PlayHistoryMusic` (`musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlayHistoryMusic_connectionId` ON `PlayHistoryMusic` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlayHistoryMusic_musicId_connectionId` ON `PlayHistoryMusic` (`musicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlayQueueMusic_musicId` ON `PlayQueueMusic` (`musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlayQueueMusic_connectionId` ON `PlayQueueMusic` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlayQueueMusic_musicId_connectionId` ON `PlayQueueMusic` (`musicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_MaximumPlayMusic_musicId` ON `MaximumPlayMusic` (`musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_MaximumPlayMusic_connectionId` ON `MaximumPlayMusic` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_MaximumPlayMusic_musicId_connectionId` ON `MaximumPlayMusic` (`musicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_NewestMusic_musicId` ON `NewestMusic` (`musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_NewestMusic_connectionId` ON `NewestMusic` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_NewestMusic_musicId_connectionId` ON `NewestMusic` (`musicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_HomeAlbum_albumId` ON `HomeAlbum` (`albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_HomeAlbum_connectionId` ON `HomeAlbum` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_HomeAlbum_albumId_connectionId` ON `HomeAlbum` (`albumId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_NewestAlbum_albumId` ON `NewestAlbum` (`albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_NewestAlbum_connectionId` ON `NewestAlbum` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_NewestAlbum_albumId_connectionId` ON `NewestAlbum` (`albumId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlayHistoryAlbum_albumId` ON `PlayHistoryAlbum` (`albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlayHistoryAlbum_connectionId` ON `PlayHistoryAlbum` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlayHistoryAlbum_albumId_connectionId` ON `PlayHistoryAlbum` (`albumId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_MaximumPlayAlbum_albumId` ON `MaximumPlayAlbum` (`albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_MaximumPlayAlbum_connectionId` ON `MaximumPlayAlbum` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_MaximumPlayAlbum_albumId_connectionId` ON `MaximumPlayAlbum` (`albumId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_FavoriteAlbum_albumId` ON `FavoriteAlbum` (`albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_FavoriteAlbum_connectionId` ON `FavoriteAlbum` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_FavoriteAlbum_albumId_connectionId` ON `FavoriteAlbum` (`albumId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistAlbum_albumId` ON `ArtistAlbum` (`albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistAlbum_connectionId` ON `ArtistAlbum` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistAlbum_artistId` ON `ArtistAlbum` (`artistId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistAlbum_artistId_connectionId` ON `ArtistAlbum` (`artistId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistAlbum_albumId_connectionId` ON `ArtistAlbum` (`albumId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_GenreAlbum_albumId` ON `GenreAlbum` (`albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_GenreAlbum_connectionId` ON `GenreAlbum` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_GenreAlbum_genreId` ON `GenreAlbum` (`genreId`)",
            "CREATE INDEX IF NOT EXISTS `index_GenreAlbum_albumId_connectionId` ON `GenreAlbum` (`albumId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_GenreAlbum_genreId_connectionId` ON `GenreAlbum` (`genreId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_FavoriteArtist_artistId` ON `FavoriteArtist` (`artistId`)",
            "CREATE INDEX IF NOT EXISTS `index_FavoriteArtist_connectionId` ON `FavoriteArtist` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_FavoriteArtist_artistId_connectionId` ON `FavoriteArtist` (`artistId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_progress_connectionId` ON `progress` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_progress_musicId_connectionId` ON `progress` (`musicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_enable_progress_connectionId` ON `xy_enable_progress` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_enable_progress_albumId_connectionId` ON `xy_enable_progress` (`albumId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_skip_time_connectionId` ON `skip_time` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_skip_time_albumId_connectionId` ON `skip_time` (`albumId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_lrc_config_connectionId` ON `xy_lrc_config` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_lrc_config_itemId_connectionId` ON `xy_lrc_config` (`itemId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_daily_recommend_history_connectionId` ON `xy_daily_recommend_history` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_daily_recommend_history_songId_connectionId` ON `xy_daily_recommend_history` (`songId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_daily_recommend_history_connectionId_timestamp` ON `xy_daily_recommend_history` (`connectionId`, `timestamp`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_daily_recommend_history_connectionId_mediaLibraryId_timestamp` ON `xy_daily_recommend_history` (`connectionId`, `mediaLibraryId`, `timestamp`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistPopularMusic_artistKey` ON `ArtistPopularMusic` (`artistKey`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistPopularMusic_musicId` ON `ArtistPopularMusic` (`musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistPopularMusic_connectionId` ON `ArtistPopularMusic` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistPopularMusic_musicId_connectionId` ON `ArtistPopularMusic` (`musicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistPopularMusic_connectionId_artistKey_cachedAt` ON `ArtistPopularMusic` (`connectionId`, `artistKey`, `cachedAt`)",
            "CREATE INDEX IF NOT EXISTS `index_SimilarMusic_sourceMusicId` ON `SimilarMusic` (`sourceMusicId`)",
            "CREATE INDEX IF NOT EXISTS `index_SimilarMusic_musicId` ON `SimilarMusic` (`musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_SimilarMusic_connectionId` ON `SimilarMusic` (`connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_SimilarMusic_sourceMusicId_connectionId` ON `SimilarMusic` (`sourceMusicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_SimilarMusic_musicId_connectionId` ON `SimilarMusic` (`musicId`, `connectionId`)",
            "CREATE INDEX IF NOT EXISTS `index_SimilarMusic_connectionId_sourceMusicId_cachedAt` ON `SimilarMusic` (`connectionId`, `sourceMusicId`, `cachedAt`)"
        ).forEach { connection.execSQL(it) }
    }
}

/**
 * v10 到 v11 的查询性能索引迁移：补齐大列表分页、年份过滤和关系表排序使用的复合索引。
 */
val Migration_10_11 = object : Migration(10, 11) {
    override fun migrate(connection: SQLiteConnection) {
        listOf(
            "CREATE INDEX IF NOT EXISTS `index_xy_music_connectionId_year` ON `xy_music` (`connectionId`, `year`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_music_connectionId_lastPlayedDate` ON `xy_music` (`connectionId`, `lastPlayedDate`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_album_connectionId_ifPlaylist_createTime` ON `xy_album` (`connectionId`, `ifPlaylist`, `createTime`)",
            "CREATE INDEX IF NOT EXISTS `index_HomeMusic_connectionId_index_musicId` ON `HomeMusic` (`connectionId`, `index`, `musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_FavoriteMusic_connectionId_index_musicId` ON `FavoriteMusic` (`connectionId`, `index`, `musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_FavoriteMusic_connectionId_ifFavorite_musicId` ON `FavoriteMusic` (`connectionId`, `ifFavorite`, `musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_AlbumMusic_connectionId_index_musicId` ON `AlbumMusic` (`connectionId`, `index`, `musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_AlbumMusic_albumId_connectionId_index` ON `AlbumMusic` (`albumId`, `connectionId`, `index`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistMusic_connectionId_index_musicId` ON `ArtistMusic` (`connectionId`, `index`, `musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistMusic_artistId_connectionId_index` ON `ArtistMusic` (`artistId`, `connectionId`, `index`)",
            "CREATE INDEX IF NOT EXISTS `index_PlaylistMusic_connectionId_index_playlistId` ON `PlaylistMusic` (`connectionId`, `index`, `playlistId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlaylistMusic_playlistId_connectionId_index` ON `PlaylistMusic` (`playlistId`, `connectionId`, `index`)",
            "CREATE INDEX IF NOT EXISTS `index_PlayHistoryMusic_connectionId_index_musicId` ON `PlayHistoryMusic` (`connectionId`, `index`, `musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlayQueueMusic_connectionId_index_musicId` ON `PlayQueueMusic` (`connectionId`, `index`, `musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_MaximumPlayMusic_connectionId_index_musicId` ON `MaximumPlayMusic` (`connectionId`, `index`, `musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_NewestMusic_connectionId_index_musicId` ON `NewestMusic` (`connectionId`, `index`, `musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistPopularMusic_connectionId_artistKey_index_musicId` ON `ArtistPopularMusic` (`connectionId`, `artistKey`, `index`, `musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_SimilarMusic_connectionId_sourceMusicId_index_musicId` ON `SimilarMusic` (`connectionId`, `sourceMusicId`, `index`, `musicId`)",
            "CREATE INDEX IF NOT EXISTS `index_HomeAlbum_connectionId_index_albumId` ON `HomeAlbum` (`connectionId`, `index`, `albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_NewestAlbum_connectionId_index_albumId` ON `NewestAlbum` (`connectionId`, `index`, `albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_PlayHistoryAlbum_connectionId_index_albumId` ON `PlayHistoryAlbum` (`connectionId`, `index`, `albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_MaximumPlayAlbum_connectionId_index_albumId` ON `MaximumPlayAlbum` (`connectionId`, `index`, `albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistAlbum_connectionId_index_albumId` ON `ArtistAlbum` (`connectionId`, `index`, `albumId`)",
            "CREATE INDEX IF NOT EXISTS `index_ArtistAlbum_artistId_connectionId_index` ON `ArtistAlbum` (`artistId`, `connectionId`, `index`)",
            "CREATE INDEX IF NOT EXISTS `index_GenreAlbum_genreId_connectionId_index` ON `GenreAlbum` (`genreId`, `connectionId`, `index`)",
            "CREATE INDEX IF NOT EXISTS `index_progress_albumId_createTime` ON `progress` (`albumId`, `createTime`)",
            "CREATE INDEX IF NOT EXISTS `index_progress_albumId_index` ON `progress` (`albumId`, `index`)",
            "CREATE INDEX IF NOT EXISTS `index_xy_daily_recommend_history_connectionId_mediaLibraryId_timestamp_recommendIndex` ON `xy_daily_recommend_history` (`connectionId`, `mediaLibraryId`, `timestamp`, `recommendIndex`)"
        ).forEach { connection.execSQL(it) }
    }
}
