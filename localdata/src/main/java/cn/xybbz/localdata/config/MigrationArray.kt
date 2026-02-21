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

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


internal object Migration1 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 数据库的升级语句
        db.execSQL("ALTER TABLE xy_connection_config DROP COLUMN ifEnable")
    }
}

internal object Migration2 : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 数据库的升级语句
        db.execSQL(
            """
                    CREATE TABLE `xy_download` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT NOT NULL, `fileName` TEXT NOT NULL, `filePath` TEXT NOT NULL, `fileSize` INTEGER NOT NULL, `tempFilePath` TEXT NOT NULL, `typeData` TEXT NOT NULL, `progress` REAL NOT NULL, `totalBytes` INTEGER NOT NULL, `downloadedBytes` INTEGER NOT NULL, `status` TEXT NOT NULL, `error` TEXT, `uid` TEXT, `title` TEXT, `cover` TEXT, `duration` INTEGER, `connectionId` INTEGER, `extend` TEXT, `music` TEXT, `updateTime` INTEGER NOT NULL, `createTime` INTEGER NOT NULL);
            """.trimIndent()
        )
    }
}

internal object Migration3 : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 数据库的升级语句
        db.execSQL("ALTER TABLE xy_music ADD COLUMN downloadUrl TEXT NOT NULL DEFAULT ''")
    }
}

internal object Migration4 : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 数据库的升级语句
        db.execSQL("ALTER TABLE PlayQueueMusic ADD COLUMN picByte BLOB")
    }
}

internal object Migration5 : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 数据库的升级语句
        db.execSQL("ALTER TABLE xy_artist ADD COLUMN backdrop TEXT")
    }
}

internal object Migration_6_7 : Migration(6, 7) {
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


internal object Migration_7_8 : Migration(7, 8) {
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
internal object Migration_8_9 : Migration(8, 9) {
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


internal object Migration_9_10 : Migration(9, 10) {
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

internal object Migration_10_11 : Migration(10, 11) {
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

internal object Migration_11_12 : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE xy_settings
            ADD COLUMN ifEnableSyncPlayProgress INTEGER NOT NULL DEFAULT 1
            """
        )
    }
}

internal object Migration_12_13 : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE xy_settings
            ADD COLUMN fadeDurationMs INTEGER NOT NULL DEFAULT 300
            """.trimIndent()
        )
    }
}

internal object Migration_13_14 : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS xy_lrc_config (
                id INTEGER PRIMARY KEY NOT NULL,
                itemId TEXT NOT NULL,
                lrcOffsetMs INTEGER NOT NULL,
                connectionId INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

internal object Migration_14_15 : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.d("sql","升级版本到15")
        // 1创建新表（不包含 picByte，但包含 PK / FK）
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS playqueuemusic_new (
                musicId TEXT NOT NULL,
                connectionId INTEGER NOT NULL,
                `index` INTEGER NOT NULL,
                cachedAt INTEGER NOT NULL,
                PRIMARY KEY(musicId, connectionId),
                FOREIGN KEY(connectionId)
                    REFERENCES xy_connection_config(id)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // 拷贝旧数据（忽略 picByte）
        db.execSQL(
            """
            INSERT INTO playqueuemusic_new (
                musicId,
                connectionId,
                `index`,
                cachedAt
            )
            SELECT
                musicId,
                connectionId,
                `index`,
                cachedAt
            FROM playqueuemusic
            """.trimIndent()
        )

        // 删除旧表
        db.execSQL("DROP TABLE playqueuemusic")

        // 重命名新表
        db.execSQL(
            "ALTER TABLE playqueuemusic_new RENAME TO playqueuemusic"
        )

        // 重新创建索引
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_playqueuemusic_musicId ON playqueuemusic(musicId)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_playqueuemusic_connectionId ON playqueuemusic(connectionId)"
        )
    }
}

internal object Migration_15_16 : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.d("sql","升级版本到16")
        // 是否转码
        db.execSQL(
            """
            ALTER TABLE xy_settings
            ADD COLUMN ifTranscoding INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        // 转码格式
        db.execSQL(
            """
            ALTER TABLE xy_settings
            ADD COLUMN transcodeFormat TEXT NOT NULL DEFAULT ''
            """.trimIndent()
        )

        // 移动网络音质
        db.execSQL(
            """
            ALTER TABLE xy_settings
            ADD COLUMN mobileNetworkAudioBitRate INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        // wifi 网络音质
        db.execSQL(
            """
            ALTER TABLE xy_settings
            ADD COLUMN wifiNetworkAudioBitRate INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

internal object Migration_16_17 : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.d("sql","升级版本到17")
        //创建新表（已删除 musicUrl，已增加 plexPlayKey）
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS xy_music_new (
                itemId TEXT NOT NULL,
                pic TEXT,
                name TEXT NOT NULL,
                downloadUrl TEXT NOT NULL,
                album TEXT NOT NULL,
                albumName TEXT,
                genreIds TEXT,
                connectionId INTEGER NOT NULL,
                artists TEXT,
                artistIds TEXT,
                albumArtist TEXT,
                albumArtistIds TEXT,
                year INTEGER,
                playedCount INTEGER NOT NULL,
                ifFavoriteStatus INTEGER NOT NULL,
                ifLyric INTEGER NOT NULL,
                lyric TEXT,
                path TEXT NOT NULL,
                bitRate INTEGER,
                sampleRate INTEGER,
                bitDepth INTEGER,
                size INTEGER,
                runTimeTicks INTEGER NOT NULL,
                container TEXT,
                codec TEXT,
                playlistItemId TEXT,
                plexPlayKey TEXT,
                lastPlayedDate INTEGER NOT NULL,
                createTime INTEGER NOT NULL,
                PRIMARY KEY(itemId),
                FOREIGN KEY(connectionId)
                    REFERENCES xy_connection_config(id)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        //拷贝旧数据（不包含 musicUrl，新字段 plexPlayKey 置 NULL）
        db.execSQL(
            """
            INSERT INTO xy_music_new (
                itemId,
                pic,
                name,
                downloadUrl,
                album,
                albumName,
                genreIds,
                connectionId,
                artists,
                artistIds,
                albumArtist,
                albumArtistIds,
                year,
                playedCount,
                ifFavoriteStatus,
                ifLyric,
                lyric,
                path,
                bitRate,
                sampleRate,
                bitDepth,
                size,
                runTimeTicks,
                container,
                codec,
                playlistItemId,
                plexPlayKey,
                lastPlayedDate,
                createTime
            )
            SELECT
                itemId,
                pic,
                name,
                downloadUrl,
                album,
                albumName,
                genreIds,
                connectionId,
                artists,
                artistIds,
                albumArtist,
                albumArtistIds,
                year,
                playedCount,
                ifFavoriteStatus,
                ifLyric,
                lyric,
                path,
                bitRate,
                sampleRate,
                bitDepth,
                size,
                runTimeTicks,
                container,
                codec,
                playlistItemId,
                NULL AS plexPlayKey,
                lastPlayedDate,
                createTime
            FROM xy_music
            """.trimIndent()
        )

        //删除旧表
        db.execSQL("DROP TABLE xy_music")

        //重命名新表
        db.execSQL("ALTER TABLE xy_music_new RENAME TO xy_music")

        //重建索引
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_xy_music_connectionId
            ON xy_music(connectionId)
            """.trimIndent()
        )
    }
}


internal object Migration_17_18 : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.d("sql","升级版本到18")
        //创建新表（带正确 DEFAULT）
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
                maxConcurrentDownloads INTEGER NOT NULL,
                ifEnableSyncPlayProgress INTEGER NOT NULL,
                fadeDurationMs INTEGER NOT NULL,
                ifTranscoding INTEGER NOT NULL,
                transcodeFormat TEXT NOT NULL DEFAULT 'mp3',
                mobileNetworkAudioBitRate INTEGER NOT NULL DEFAULT 192000,
                wifiNetworkAudioBitRate INTEGER NOT NULL
            )
            """.trimIndent()
        )

        //拷贝旧数据（对旧数据兜底）
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
                maxConcurrentDownloads,
                ifEnableSyncPlayProgress,
                fadeDurationMs,
                ifTranscoding,
                transcodeFormat,
                mobileNetworkAudioBitRate,
                wifiNetworkAudioBitRate
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
                maxConcurrentDownloads,
                ifEnableSyncPlayProgress,
                fadeDurationMs,
                ifTranscoding,
                CASE
                    WHEN transcodeFormat IS NULL OR transcodeFormat = ''
                    THEN 'mp3'
                    ELSE transcodeFormat
                END,
                CASE
                    WHEN mobileNetworkAudioBitRate IS NULL OR mobileNetworkAudioBitRate = 0
                    THEN 192000
                    ELSE mobileNetworkAudioBitRate
                END,
                wifiNetworkAudioBitRate
            FROM xy_settings
            """.trimIndent()
        )

        //删除旧表
        db.execSQL("DROP TABLE xy_settings")

        //重命名
        db.execSQL("ALTER TABLE xy_settings_new RENAME TO xy_settings")

        db.execSQL("""
            CREATE INDEX IF NOT EXISTS
            index_xy_recent_recommend_history_connectionId
            ON xy_recent_recommend_history(connectionId)
        """)
    }
}

internal val Migration_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            ALTER TABLE xy_settings
            ADD COLUMN playSessionId TEXT NOT NULL
            DEFAULT ''
        """)
    }
}

internal val Migration_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            ALTER TABLE xy_settings
            ADD COLUMN dataSourceType TEXT
        """)
    }
}

internal val Migration_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            ALTER TABLE xy_settings
            ADD COLUMN ifEnableEdgeDownload INTEGER NOT NULL
            DEFAULT 1
        """)
    }
}

internal val Migration_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {

        //创建新表（不包含 indexNumber）
        db.execSQL("""
        CREATE TABLE xy_artist_new (
            artistId TEXT NOT NULL PRIMARY KEY,
            pic TEXT,
            backdrop TEXT,
            describe TEXT,
            name TEXT,
            sortName TEXT,
            connectionId INTEGER NOT NULL,
            musicCount INTEGER,
            albumCount INTEGER,
            selectChat TEXT NOT NULL,
            FOREIGN KEY(connectionId)
            REFERENCES xy_connection_config(id)
            ON DELETE CASCADE
        )
        """)

        //迁移旧数据（不拷贝 indexNumber）
        db.execSQL("""
        INSERT INTO xy_artist_new (
            artistId, pic, backdrop, describe, name,
            sortName, connectionId, musicCount,
            albumCount, selectChat
        )
        SELECT
            artistId, pic, backdrop, describe, name,
            sortName, connectionId, musicCount,
            albumCount, selectChat
        FROM xy_artist
        """)

        //删除旧表
        db.execSQL("DROP TABLE xy_artist")

        //重命名
        db.execSQL("""
            ALTER TABLE xy_artist_new
            RENAME TO xy_artist
        """)

        //重建索引
        db.execSQL("""
            CREATE INDEX index_xy_artist_connectionId
            ON xy_artist(connectionId)
        """)
    }
}


internal val Migration_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            ALTER TABLE xy_connection_config
            ADD COLUMN ifEnabledDownload INTEGER NOT NULL DEFAULT 1
        """)

        db.execSQL("""
            ALTER TABLE xy_connection_config
            ADD COLUMN ifEnabledDelete INTEGER NOT NULL DEFAULT 0
        """)
    }
}

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            ALTER TABLE xy_settings
            ADD COLUMN themeType TEXT NOT NULL DEFAULT 'SYSTEM'
        """)

        db.execSQL("""
            ALTER TABLE xy_settings
            ADD COLUMN isDynamic INTEGER NOT NULL DEFAULT 0
        """)
    }
}

val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            ALTER TABLE xy_background_config
            ADD COLUMN ifEnabled INTEGER NOT NULL DEFAULT 0
        """)
    }
}

val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
    ALTER TABLE xy_connection_config
    ADD COLUMN ifForceLogin INTEGER NOT NULL DEFAULT 0
""")
    }
}