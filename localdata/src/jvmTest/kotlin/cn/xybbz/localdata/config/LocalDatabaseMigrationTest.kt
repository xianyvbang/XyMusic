package cn.xybbz.localdata.config

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 本地主库 v9 到 v11 迁移测试。
 */
class LocalDatabaseMigrationTest {
    /**
     * 迁移应保留合法引用数据，并过滤旧库中已经存在的孤儿行。
     */
    @Test
    fun migrationNineToElevenKeepsValidRowsAndDropsOrphans() {
        withMigratedDatabase(
            seedVersionNineData = { connection ->
                seedValidReferenceGraph(connection)
                seedValidRelationRows(connection)
                seedOrphanRows(connection)
            },
            verifyMigratedData = { connection ->
                assertEquals(2, countRows(connection, "xy_music"))
                assertEquals(1, countRows(connection, "xy_album"))
                assertEquals(1, countRows(connection, "xy_artist"))
                assertEquals(1, countRows(connection, "xy_genre"))
                assertEquals(1, countRows(connection, "xy_library"))
                assertEquals(2, countRows(connection, "xy_settings"))
                assertEquals(1, countRows(connection, "xy_settings", "id = 2 AND connectionId IS NULL"))

                listOf(
                    "HomeMusic",
                    "FavoriteMusic",
                    "AlbumMusic",
                    "ArtistMusic",
                    "PlaylistMusic",
                    "PlayHistoryMusic",
                    "PlayQueueMusic",
                    "MaximumPlayMusic",
                    "NewestMusic",
                    "HomeAlbum",
                    "NewestAlbum",
                    "PlayHistoryAlbum",
                    "MaximumPlayAlbum",
                    "FavoriteAlbum",
                    "ArtistAlbum",
                    "GenreAlbum",
                    "FavoriteArtist",
                    "progress",
                    "xy_enable_progress",
                    "skip_time",
                    "xy_lrc_config",
                    "xy_daily_recommend_history",
                    "ArtistPopularMusic",
                    "SimilarMusic",
                    "remote_current",
                    "search_history",
                    "xy_data_count",
                    "xy_player",
                ).forEach { tableName ->
                    assertEquals(1, countRows(connection, tableName), "$tableName 应只保留一条合法数据")
                }

                assertEquals(0, countRows(connection, "xy_music", "connectionId = 999"))
                assertEquals(0, countRows(connection, "xy_album", "connectionId = 999"))
                assertEquals(0, countRows(connection, "xy_artist", "connectionId = 999"))
                assertEquals(0, countRows(connection, "xy_genre", "connectionId = 999"))
                assertEquals(0, countRows(connection, "xy_library", "connectionId = 999"))
                assertEquals(1, countRows(connection, "ArtistPopularMusic", "artistKey = 'artist-name'"))
                assertEquals(0, countRows(connection, "ArtistPopularMusic", "artistKey = 'ghost-artist'"))
            }
        )
    }

    /**
     * 删除音乐父记录后，所有音乐关系和缓存行应由 SQLite 级联删除。
     */
    @Test
    fun deletingMusicCascadesMusicScopedRows() {
        withMigratedDatabase(
            seedVersionNineData = { connection ->
                seedValidReferenceGraph(connection)
                seedMusicCascadeRows(connection)
            },
            verifyMigratedData = { connection ->
                connection.execSQL("DELETE FROM `xy_music` WHERE `itemId` = 'music-1' AND `connectionId` = 1")

                assertEquals(1, countRows(connection, "xy_music"))
                listOf(
                    "HomeMusic",
                    "FavoriteMusic",
                    "AlbumMusic",
                    "ArtistMusic",
                    "PlaylistMusic",
                    "PlayHistoryMusic",
                    "PlayQueueMusic",
                    "MaximumPlayMusic",
                    "NewestMusic",
                    "progress",
                    "xy_lrc_config",
                    "xy_daily_recommend_history",
                    "ArtistPopularMusic",
                    "SimilarMusic",
                ).forEach { tableName ->
                    assertEquals(0, countRows(connection, tableName), "$tableName 应随音乐删除清空")
                }
            }
        )
    }

    /**
     * 删除专辑父记录后，专辑关系、歌单音乐、跳过时间和专辑播放历史开关应级联删除。
     */
    @Test
    fun deletingAlbumCascadesAlbumScopedRows() {
        withMigratedDatabase(
            seedVersionNineData = { connection ->
                seedValidReferenceGraph(connection)
                seedAlbumCascadeRows(connection)
            },
            verifyMigratedData = { connection ->
                connection.execSQL("DELETE FROM `xy_album` WHERE `itemId` = 'album-1' AND `connectionId` = 1")

                assertEquals(0, countRows(connection, "xy_album"))
                listOf(
                    "AlbumMusic",
                    "PlaylistMusic",
                    "HomeAlbum",
                    "NewestAlbum",
                    "PlayHistoryAlbum",
                    "MaximumPlayAlbum",
                    "FavoriteAlbum",
                    "ArtistAlbum",
                    "GenreAlbum",
                    "xy_enable_progress",
                    "skip_time",
                ).forEach { tableName ->
                    assertEquals(0, countRows(connection, tableName), "$tableName 应随专辑删除清空")
                }
                assertEquals(1, countRows(connection, "progress"))
            }
        )
    }

    /**
     * 删除连接配置后，该连接的本地数据应级联清理，设置表连接 ID 应置空。
     */
    @Test
    fun deletingConnectionCascadesLocalRowsAndNullsSettings() {
        withMigratedDatabase(
            seedVersionNineData = { connection ->
                seedValidReferenceGraph(connection)
                seedValidRelationRows(connection)
            },
            verifyMigratedData = { connection ->
                connection.execSQL("DELETE FROM `xy_connection_config` WHERE `id` = 1")

                listOf(
                    "xy_music",
                    "xy_album",
                    "xy_artist",
                    "xy_genre",
                    "xy_library",
                    "HomeMusic",
                    "FavoriteMusic",
                    "AlbumMusic",
                    "ArtistMusic",
                    "PlaylistMusic",
                    "PlayHistoryMusic",
                    "PlayQueueMusic",
                    "MaximumPlayMusic",
                    "NewestMusic",
                    "HomeAlbum",
                    "NewestAlbum",
                    "PlayHistoryAlbum",
                    "MaximumPlayAlbum",
                    "FavoriteAlbum",
                    "ArtistAlbum",
                    "GenreAlbum",
                    "FavoriteArtist",
                    "progress",
                    "xy_enable_progress",
                    "skip_time",
                    "xy_lrc_config",
                    "xy_daily_recommend_history",
                    "ArtistPopularMusic",
                    "SimilarMusic",
                    "remote_current",
                    "search_history",
                    "xy_data_count",
                    "xy_player",
                ).forEach { tableName ->
                    assertEquals(0, countRows(connection, tableName), "$tableName 应随连接删除清空")
                }
                assertEquals(1, countRows(connection, "xy_settings"))
                assertEquals(1, countRows(connection, "xy_settings", "id = 1 AND connectionId IS NULL"))
            }
        )
    }

    /**
     * 打开临时 SQLite 数据库，创建 v9 schema 后运行 v9→v11 迁移。
     */
    private fun withMigratedDatabase(
        seedVersionNineData: (SQLiteConnection) -> Unit,
        verifyMigratedData: (SQLiteConnection) -> Unit,
    ) {
        val dbFile = Files.createTempFile("xy-localdata-migration-test", ".db").toFile()

        try {
            BundledSQLiteDriver().open(dbFile.absolutePath).use { connection ->
                createVersionNineSchema(connection)
                seedVersionNineData(connection)
                connection.execSQL("PRAGMA foreign_keys = ON")

                Migration_9_10.migrate(connection)
                Migration_10_11.migrate(connection)

                connection.execSQL("PRAGMA foreign_keys = ON")
                assertForeignKeyCheckIsEmpty(connection)
                assertPerformanceIndexesExist(connection)
                verifyMigratedData(connection)
                assertForeignKeyCheckIsEmpty(connection)
            }
        } finally {
            dbFile.delete()
        }
    }

    /**
     * 创建迁移前 v9 旧库完整表结构。
     */
    private fun createVersionNineSchema(connection: SQLiteConnection) {
        versionNineCreateSqlList().forEach(connection::execSQL)
    }

    /**
     * 返回迁移测试使用的 v9 建表 SQL，避免依赖被 git 忽略的 Room schema 目录。
     */
    private fun versionNineCreateSqlList(): List<String> {
        return listOf(
            """
            CREATE TABLE IF NOT EXISTS `xy_music` (`itemId` TEXT NOT NULL, `pic` TEXT, `name` TEXT NOT NULL, `downloadUrl` TEXT NOT NULL, `album` TEXT NOT NULL, `albumName` TEXT, `genreIds` TEXT, `connectionId` INTEGER NOT NULL, `artists` TEXT, `artistIds` TEXT, `albumArtist` TEXT, `albumArtistIds` TEXT, `year` INTEGER, `playedCount` INTEGER NOT NULL, `ifFavoriteStatus` INTEGER NOT NULL, `ifLyric` INTEGER NOT NULL, `lyric` TEXT, `path` TEXT NOT NULL, `bitRate` INTEGER, `sampleRate` INTEGER, `bitDepth` INTEGER, `size` INTEGER, `runTimeTicks` INTEGER NOT NULL, `container` TEXT, `codec` TEXT, `playlistItemId` TEXT, `plexPlayKey` TEXT, `lastPlayedDate` INTEGER NOT NULL, `createTime` INTEGER NOT NULL, PRIMARY KEY(`itemId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_album` (`itemId` TEXT NOT NULL, `pic` TEXT, `name` TEXT NOT NULL, `artists` TEXT, `artistIds` TEXT, `genreIds` TEXT, `connectionId` INTEGER NOT NULL, `year` INTEGER, `premiereDate` INTEGER, `ifPlaylist` INTEGER NOT NULL, `musicCount` INTEGER NOT NULL, `createTime` INTEGER NOT NULL, PRIMARY KEY(`itemId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_settings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ifEnableEdgeDownload` INTEGER NOT NULL, `cacheUpperLimit` TEXT NOT NULL, `ifDesktopLyrics` INTEGER NOT NULL, `doubleSpeed` REAL NOT NULL, `connectionId` INTEGER, `dataSourceType` TEXT, `ifEnableAlbumHistory` INTEGER NOT NULL, `ifHandleAudioFocus` INTEGER NOT NULL, `languageType` TEXT, `latestVersionTime` INTEGER NOT NULL, `latestVersion` TEXT NOT NULL, `lasestApkUrl` TEXT NOT NULL, `maxConcurrentDownloads` INTEGER NOT NULL, `ifEnableSyncPlayProgress` INTEGER NOT NULL, `fadeDurationMs` INTEGER NOT NULL, `ifTranscoding` INTEGER NOT NULL, `transcodeFormat` TEXT NOT NULL, `mobileNetworkAudioBitRate` INTEGER NOT NULL, `wifiNetworkAudioBitRate` INTEGER NOT NULL, `ifPriorityMusicApi` INTEGER NOT NULL, `customLrcSingleApi` TEXT NOT NULL, `customLrcApiAuth` TEXT NOT NULL, `customCoverApi` TEXT NOT NULL, `playSessionId` TEXT NOT NULL, `themeType` TEXT NOT NULL, `imageFilePath` TEXT, `jvmVolume` INTEGER, `cacheFilePath` TEXT NOT NULL, `ifSyncPasswordsByICloud` INTEGER NOT NULL)
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `skip_time` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `albumId` TEXT NOT NULL, `headTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `connectionId` INTEGER NOT NULL)
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `remote_current` (`id` TEXT NOT NULL, `nextKey` INTEGER NOT NULL, `prevKey` INTEGER NOT NULL, `total` INTEGER NOT NULL, `refresh` INTEGER NOT NULL, `connectionId` INTEGER NOT NULL, `createTime` INTEGER NOT NULL, PRIMARY KEY(`id`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `search_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `searchQuery` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `createTime` INTEGER NOT NULL)
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `progress` (`musicId` TEXT NOT NULL, `musicName` TEXT NOT NULL, `albumId` TEXT NOT NULL, `progress` INTEGER NOT NULL, `progressPercentage` INTEGER NOT NULL, `index` INTEGER NOT NULL, `connectionId` INTEGER NOT NULL, `createTime` INTEGER NOT NULL, PRIMARY KEY(`musicId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_artist` (`artistId` TEXT NOT NULL, `pic` TEXT, `backdrop` TEXT, `describe` TEXT, `name` TEXT, `sortName` TEXT, `connectionId` INTEGER NOT NULL, `musicCount` INTEGER, `albumCount` INTEGER, `selectChat` TEXT NOT NULL, PRIMARY KEY(`artistId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_enable_progress` (`albumId` TEXT NOT NULL, `ifEnableAlbumHistory` INTEGER NOT NULL, `connectionId` INTEGER NOT NULL, PRIMARY KEY(`albumId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_library` (`id` TEXT NOT NULL, `collectionType` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_player` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `connectionId` INTEGER, `dataType` TEXT NOT NULL, `musicId` TEXT NOT NULL, `headTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `playerType` TEXT NOT NULL, `pageNum` INTEGER NOT NULL, `pageSize` INTEGER NOT NULL, `ifSkip` INTEGER NOT NULL, `albumId` TEXT NOT NULL, `artistId` TEXT)
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_connection_config` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `serverId` TEXT NOT NULL, `serverName` TEXT NOT NULL, `serverVersion` TEXT NOT NULL, `deviceId` TEXT NOT NULL, `name` TEXT NOT NULL, `address` TEXT NOT NULL, `type` TEXT NOT NULL, `userId` TEXT NOT NULL, `username` TEXT NOT NULL, `currentPassword` TEXT NOT NULL, `iv` TEXT NOT NULL, `credentialStoreType` TEXT NOT NULL, `libraryIds` TEXT, `extendInfo` TEXT, `lastLoginTime` INTEGER NOT NULL, `updateTime` INTEGER NOT NULL, `createTime` INTEGER NOT NULL, `ifEnabledDownload` INTEGER NOT NULL, `ifEnabledDelete` INTEGER NOT NULL)
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_era_item` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `era` INTEGER NOT NULL, `years` TEXT NOT NULL)
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_genre` (`itemId` TEXT NOT NULL, `pic` TEXT NOT NULL, `name` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `createTime` INTEGER NOT NULL, PRIMARY KEY(`itemId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `AlbumMusic` (`albumId` TEXT NOT NULL, `musicId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`albumId`, `musicId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `ArtistAlbum` (`artistId` TEXT NOT NULL, `albumId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`artistId`, `albumId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `ArtistMusic` (`artistId` TEXT NOT NULL, `musicId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`artistId`, `musicId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `FavoriteMusic` (`musicId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `ifFavorite` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`musicId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `GenreAlbum` (`genreId` TEXT NOT NULL, `albumId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`albumId`, `genreId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `HomeAlbum` (`albumId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`albumId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `HomeMusic` (`musicId` TEXT NOT NULL, `pic` TEXT, `name` TEXT NOT NULL, `artists` TEXT, `album` TEXT NOT NULL, `albumName` TEXT, `codec` TEXT, `bitRate` INTEGER, `runTimeTicks` INTEGER NOT NULL DEFAULT 0, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`musicId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `MaximumPlayMusic` (`musicId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`musicId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `NewestAlbum` (`albumId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`albumId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `NewestMusic` (`musicId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`musicId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `PlayHistoryMusic` (`musicId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`musicId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `PlaylistMusic` (`playlistId` TEXT NOT NULL, `musicId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`playlistId`, `musicId`, `connectionId`, `index`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `PlayQueueMusic` (`musicId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`musicId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_data_count` (`connectionId` INTEGER NOT NULL, `musicCount` INTEGER, `albumCount` INTEGER, `artistCount` INTEGER, `playlistCount` INTEGER, `genreCount` INTEGER, `favoriteCount` INTEGER, `createTime` INTEGER NOT NULL, PRIMARY KEY(`connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `PlayHistoryAlbum` (`albumId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`albumId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `MaximumPlayAlbum` (`albumId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`albumId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `FavoriteAlbum` (`albumId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `ifFavorite` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`albumId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `FavoriteArtist` (`artistId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `ifFavorite` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`artistId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_daily_recommend_history` (`songId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `mediaLibraryId` TEXT, `recommendIndex` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`songId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_proxy_config` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `enabled` INTEGER NOT NULL, `address` TEXT NOT NULL)
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `xy_lrc_config` (`id` INTEGER NOT NULL, `itemId` TEXT NOT NULL, `lrcOffsetMs` INTEGER NOT NULL, `connectionId` INTEGER NOT NULL, PRIMARY KEY(`id`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `ArtistPopularMusic` (`artistKey` TEXT NOT NULL, `musicId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`artistKey`, `musicId`, `connectionId`))
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS `SimilarMusic` (`sourceMusicId` TEXT NOT NULL, `musicId` TEXT NOT NULL, `connectionId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`sourceMusicId`, `musicId`, `connectionId`))
            """.trimIndent(),
        )
    }

    /**
     * 写入一组合法父表数据，作为迁移和级联测试的基础引用图。
     */
    private fun seedValidReferenceGraph(connection: SQLiteConnection) {
        insertConnection(connection, id = 1)
        insertMusic(connection, itemId = "music-1", connectionId = 1)
        insertMusic(connection, itemId = "music-2", connectionId = 1)
        insertAlbum(connection, itemId = "album-1", connectionId = 1)
        insertArtist(connection, artistId = "artist-1", connectionId = 1)
        insertGenre(connection, itemId = "genre-1", connectionId = 1)
        insertLibrary(connection, id = "library-1", connectionId = 1)
    }

    /**
     * 写入一组覆盖主要关系表的合法数据。
     */
    private fun seedValidRelationRows(connection: SQLiteConnection) {
        seedMusicCascadeRows(connection)
        listOf("HomeAlbum", "NewestAlbum", "PlayHistoryAlbum", "MaximumPlayAlbum").forEach { tableName ->
            insertAlbumCacheRow(connection, tableName = tableName, albumId = "album-1", connectionId = 1)
        }
        insertFavoriteAlbum(connection, albumId = "album-1", connectionId = 1)
        insertArtistAlbum(connection, artistId = "artist-1", albumId = "album-1", connectionId = 1)
        insertGenreAlbum(connection, genreId = "genre-1", albumId = "album-1", connectionId = 1)
        insertFavoriteArtist(connection, artistId = "artist-1", connectionId = 1)
        insertEnableProgress(connection, albumId = "album-1", connectionId = 1)
        insertSkipTime(connection, id = 1, albumId = "album-1", connectionId = 1)
        insertRemoteCurrent(connection, id = "remote-1", connectionId = 1)
        insertSearchHistory(connection, id = 1, searchQuery = "query", connectionId = 1)
        insertDataCount(connection, connectionId = 1)
        insertPlayer(connection, id = 1, connectionId = 1)
        insertSettings(connection, id = 1, connectionId = 1)
    }

    /**
     * 写入音乐删除级联需要覆盖的合法关系数据。
     */
    private fun seedMusicCascadeRows(connection: SQLiteConnection) {
        insertHomeMusic(connection, musicId = "music-1", connectionId = 1)
        insertFavoriteMusic(connection, musicId = "music-1", connectionId = 1)
        insertAlbumMusic(connection, albumId = "album-1", musicId = "music-1", connectionId = 1)
        insertArtistMusic(connection, artistId = "artist-1", musicId = "music-1", connectionId = 1)
        insertPlaylistMusic(connection, playlistId = "album-1", musicId = "music-1", connectionId = 1)
        listOf("PlayHistoryMusic", "PlayQueueMusic", "MaximumPlayMusic", "NewestMusic").forEach { tableName ->
            insertMusicCacheRow(connection, tableName = tableName, musicId = "music-1", connectionId = 1)
        }
        insertProgress(connection, musicId = "music-1", albumId = "album-1", connectionId = 1)
        insertLrcConfig(connection, id = 1, itemId = "music-1", connectionId = 1)
        insertDailyRecommendHistory(connection, songId = "music-1", connectionId = 1)
        insertArtistPopularMusic(connection, artistKey = "artist-name", musicId = "music-1", connectionId = 1)
        insertSimilarMusic(connection, sourceMusicId = "music-1", musicId = "music-2", connectionId = 1)
    }

    /**
     * 写入专辑删除级联需要覆盖的合法关系数据。
     */
    private fun seedAlbumCascadeRows(connection: SQLiteConnection) {
        insertAlbumMusic(connection, albumId = "album-1", musicId = "music-1", connectionId = 1)
        insertPlaylistMusic(connection, playlistId = "album-1", musicId = "music-1", connectionId = 1)
        listOf("HomeAlbum", "NewestAlbum", "PlayHistoryAlbum", "MaximumPlayAlbum").forEach { tableName ->
            insertAlbumCacheRow(connection, tableName = tableName, albumId = "album-1", connectionId = 1)
        }
        insertFavoriteAlbum(connection, albumId = "album-1", connectionId = 1)
        insertArtistAlbum(connection, artistId = "artist-1", albumId = "album-1", connectionId = 1)
        insertGenreAlbum(connection, genreId = "genre-1", albumId = "album-1", connectionId = 1)
        insertFavoriteArtist(connection, artistId = "artist-1", connectionId = 1)
        insertEnableProgress(connection, albumId = "album-1", connectionId = 1)
        insertSkipTime(connection, id = 1, albumId = "album-1", connectionId = 1)
        insertProgress(connection, musicId = "music-1", albumId = "album-1", connectionId = 1)
    }

    /**
     * 写入旧库允许存在、迁移后必须过滤或修正的孤儿数据。
     */
    private fun seedOrphanRows(connection: SQLiteConnection) {
        insertMusic(connection, itemId = "music-bad-connection", connectionId = 999)
        insertAlbum(connection, itemId = "album-bad-connection", connectionId = 999)
        insertArtist(connection, artistId = "artist-bad-connection", connectionId = 999)
        insertGenre(connection, itemId = "genre-bad-connection", connectionId = 999)
        insertLibrary(connection, id = "library-bad-connection", connectionId = 999)
        insertSettings(connection, id = 2, connectionId = 999)
        insertRemoteCurrent(connection, id = "remote-orphan", connectionId = 999)
        insertSearchHistory(connection, id = 2, searchQuery = "orphan", connectionId = 999)
        insertDataCount(connection, connectionId = 999)
        insertPlayer(connection, id = 2, connectionId = 999)

        insertHomeMusic(connection, musicId = "missing-home", connectionId = 1)
        insertFavoriteMusic(connection, musicId = "missing-favorite", connectionId = 1)
        insertAlbumMusic(connection, albumId = "missing-album", musicId = "music-1", connectionId = 1)
        insertAlbumMusic(connection, albumId = "album-1", musicId = "missing-album-music", connectionId = 1)
        insertArtistMusic(connection, artistId = "missing-artist", musicId = "music-1", connectionId = 1)
        insertArtistMusic(connection, artistId = "artist-1", musicId = "missing-artist-music", connectionId = 1)
        insertPlaylistMusic(connection, playlistId = "missing-playlist", musicId = "music-1", connectionId = 1)
        insertPlaylistMusic(connection, playlistId = "album-1", musicId = "missing-playlist-music", connectionId = 1)
        listOf("PlayHistoryMusic", "PlayQueueMusic", "MaximumPlayMusic", "NewestMusic").forEach { tableName ->
            insertMusicCacheRow(connection, tableName = tableName, musicId = "missing-$tableName", connectionId = 1)
        }
        insertProgress(connection, musicId = "missing-progress", albumId = "album-1", connectionId = 1)
        insertLrcConfig(connection, id = 2, itemId = "missing-lrc", connectionId = 1)
        insertDailyRecommendHistory(connection, songId = "missing-daily", connectionId = 1)
        insertArtistPopularMusic(connection, artistKey = "ghost-artist", musicId = "missing-popular", connectionId = 1)
        insertSimilarMusic(connection, sourceMusicId = "missing-source", musicId = "music-2", connectionId = 1)
        insertSimilarMusic(connection, sourceMusicId = "music-1", musicId = "missing-target", connectionId = 1)

        listOf("HomeAlbum", "NewestAlbum", "PlayHistoryAlbum", "MaximumPlayAlbum").forEach { tableName ->
            insertAlbumCacheRow(connection, tableName = tableName, albumId = "missing-$tableName", connectionId = 1)
        }
        insertFavoriteAlbum(connection, albumId = "missing-favorite-album", connectionId = 1)
        insertArtistAlbum(connection, artistId = "artist-1", albumId = "missing-artist-album", connectionId = 1)
        insertArtistAlbum(connection, artistId = "missing-artist-album-artist", albumId = "album-1", connectionId = 1)
        insertGenreAlbum(connection, genreId = "genre-1", albumId = "missing-genre-album", connectionId = 1)
        insertGenreAlbum(connection, genreId = "missing-genre", albumId = "album-1", connectionId = 1)
        insertFavoriteArtist(connection, artistId = "missing-favorite-artist", connectionId = 1)
        insertEnableProgress(connection, albumId = "missing-enable-progress", connectionId = 1)
        insertSkipTime(connection, id = 2, albumId = "missing-skip", connectionId = 1)
    }

    /**
     * 插入连接配置测试数据。
     */
    private fun insertConnection(connection: SQLiteConnection, id: Long) {
        connection.execSQL(
            """
            INSERT INTO `xy_connection_config` (
                `id`, `serverId`, `serverName`, `serverVersion`, `deviceId`, `name`,
                `address`, `type`, `userId`, `username`, `currentPassword`, `iv`,
                `credentialStoreType`, `libraryIds`, `extendInfo`, `lastLoginTime`,
                `updateTime`, `createTime`, `ifEnabledDownload`, `ifEnabledDelete`
            ) VALUES (
                $id, 'server-$id', 'server', '1.0', 'device', 'connection-$id',
                'https://example.test', 'SUBSONIC', 'user', 'name', '', '',
                'NONE', NULL, NULL, 1, 1, 1, 0, 0
            )
            """.trimIndent()
        )
    }

    /**
     * 插入音乐父表测试数据。
     */
    private fun insertMusic(connection: SQLiteConnection, itemId: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `xy_music` (
                `itemId`, `name`, `downloadUrl`, `album`, `connectionId`,
                `playedCount`, `ifFavoriteStatus`, `ifLyric`, `path`,
                `runTimeTicks`, `lastPlayedDate`, `createTime`
            ) VALUES (
                '$itemId', '$itemId-name', '', 'album-1', $connectionId,
                0, 0, 0, '', 1000, 0, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入专辑父表测试数据。
     */
    private fun insertAlbum(connection: SQLiteConnection, itemId: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `xy_album` (
                `itemId`, `name`, `connectionId`, `ifPlaylist`, `musicCount`, `createTime`
            ) VALUES (
                '$itemId', '$itemId-name', $connectionId, 1, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入艺术家父表测试数据。
     */
    private fun insertArtist(connection: SQLiteConnection, artistId: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `xy_artist` (
                `artistId`, `name`, `connectionId`, `selectChat`
            ) VALUES (
                '$artistId', '$artistId-name', $connectionId, ''
            )
            """.trimIndent()
        )
    }

    /**
     * 插入风格父表测试数据。
     */
    private fun insertGenre(connection: SQLiteConnection, itemId: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `xy_genre` (
                `itemId`, `pic`, `name`, `connectionId`, `createTime`
            ) VALUES (
                '$itemId', '', '$itemId-name', $connectionId, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入媒体库父表测试数据。
     */
    private fun insertLibrary(connection: SQLiteConnection, id: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `xy_library` (
                `id`, `collectionType`, `connectionId`, `name`
            ) VALUES (
                '$id', 'music', $connectionId, '$id-name'
            )
            """.trimIndent()
        )
    }

    /**
     * 插入设置表测试数据。
     */
    private fun insertSettings(connection: SQLiteConnection, id: Long, connectionId: Long?) {
        val connectionSql = connectionId?.toString() ?: "NULL"
        connection.execSQL(
            """
            INSERT INTO `xy_settings` (
                `id`, `ifEnableEdgeDownload`, `cacheUpperLimit`, `ifDesktopLyrics`, `doubleSpeed`,
                `connectionId`, `dataSourceType`, `ifEnableAlbumHistory`, `ifHandleAudioFocus`,
                `languageType`, `latestVersionTime`, `latestVersion`, `lasestApkUrl`,
                `maxConcurrentDownloads`, `ifEnableSyncPlayProgress`, `fadeDurationMs`,
                `ifTranscoding`, `transcodeFormat`, `mobileNetworkAudioBitRate`,
                `wifiNetworkAudioBitRate`, `ifPriorityMusicApi`, `customLrcSingleApi`,
                `customLrcApiAuth`, `customCoverApi`, `playSessionId`, `themeType`,
                `imageFilePath`, `jvmVolume`, `cacheFilePath`, `ifSyncPasswordsByICloud`
            ) VALUES (
                $id, 0, 'GB_1', 0, 1.0,
                $connectionSql, 'SUBSONIC', 1, 1,
                'zh', 0, '', '',
                3, 0, 0,
                0, '', 0,
                0, 0, '',
                '', '', '', 'SYSTEM',
                NULL, NULL, '', 0
            )
            """.trimIndent()
        )
    }

    /**
     * 插入远程分页游标测试数据。
     */
    private fun insertRemoteCurrent(connection: SQLiteConnection, id: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `remote_current` (
                `id`, `nextKey`, `prevKey`, `total`, `refresh`, `connectionId`, `createTime`
            ) VALUES (
                '$id', 1, 0, 1, 0, $connectionId, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入搜索历史测试数据。
     */
    private fun insertSearchHistory(
        connection: SQLiteConnection,
        id: Long,
        searchQuery: String,
        connectionId: Long,
    ) {
        connection.execSQL(
            """
            INSERT INTO `search_history` (`id`, `searchQuery`, `connectionId`, `createTime`)
            VALUES ($id, '$searchQuery', $connectionId, 1)
            """.trimIndent()
        )
    }

    /**
     * 插入统计缓存测试数据。
     */
    private fun insertDataCount(connection: SQLiteConnection, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `xy_data_count` (
                `connectionId`, `musicCount`, `albumCount`, `artistCount`,
                `playlistCount`, `genreCount`, `favoriteCount`, `createTime`
            ) VALUES (
                $connectionId, 1, 1, 1, 1, 1, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入播放器配置测试数据。
     */
    private fun insertPlayer(connection: SQLiteConnection, id: Long, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `xy_player` (
                `id`, `connectionId`, `dataType`, `musicId`, `headTime`, `endTime`,
                `playerType`, `pageNum`, `pageSize`, `ifSkip`, `albumId`, `artistId`
            ) VALUES (
                $id, $connectionId, 'MUSIC', '', 0, 0,
                'SEQUENTIAL_PLAYBACK', 0, 20, 0, '', ''
            )
            """.trimIndent()
        )
    }

    /**
     * 插入首页音乐缓存测试数据。
     */
    private fun insertHomeMusic(connection: SQLiteConnection, musicId: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `HomeMusic` (
                `musicId`, `name`, `album`, `connectionId`, `index`, `cachedAt`, `runTimeTicks`
            ) VALUES (
                '$musicId', '$musicId-name', 'album-1', $connectionId, 1, 1, 1000
            )
            """.trimIndent()
        )
    }

    /**
     * 插入收藏音乐缓存测试数据。
     */
    private fun insertFavoriteMusic(connection: SQLiteConnection, musicId: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `FavoriteMusic` (
                `musicId`, `connectionId`, `ifFavorite`, `index`, `cachedAt`
            ) VALUES (
                '$musicId', $connectionId, 1, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入专辑音乐关系测试数据。
     */
    private fun insertAlbumMusic(
        connection: SQLiteConnection,
        albumId: String,
        musicId: String,
        connectionId: Long,
    ) {
        connection.execSQL(
            """
            INSERT INTO `AlbumMusic` (
                `albumId`, `musicId`, `connectionId`, `index`, `cachedAt`
            ) VALUES (
                '$albumId', '$musicId', $connectionId, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入艺术家音乐关系测试数据。
     */
    private fun insertArtistMusic(
        connection: SQLiteConnection,
        artistId: String,
        musicId: String,
        connectionId: Long,
    ) {
        connection.execSQL(
            """
            INSERT INTO `ArtistMusic` (
                `artistId`, `musicId`, `connectionId`, `index`, `cachedAt`
            ) VALUES (
                '$artistId', '$musicId', $connectionId, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入歌单音乐关系测试数据。
     */
    private fun insertPlaylistMusic(
        connection: SQLiteConnection,
        playlistId: String,
        musicId: String,
        connectionId: Long,
    ) {
        connection.execSQL(
            """
            INSERT INTO `PlaylistMusic` (
                `playlistId`, `musicId`, `connectionId`, `index`, `cachedAt`
            ) VALUES (
                '$playlistId', '$musicId', $connectionId, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入通用音乐缓存表测试数据。
     */
    private fun insertMusicCacheRow(
        connection: SQLiteConnection,
        tableName: String,
        musicId: String,
        connectionId: Long,
    ) {
        connection.execSQL(
            """
            INSERT INTO `$tableName` (
                `musicId`, `connectionId`, `index`, `cachedAt`
            ) VALUES (
                '$musicId', $connectionId, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入通用专辑缓存表测试数据。
     */
    private fun insertAlbumCacheRow(
        connection: SQLiteConnection,
        tableName: String,
        albumId: String,
        connectionId: Long,
    ) {
        connection.execSQL(
            """
            INSERT INTO `$tableName` (
                `albumId`, `connectionId`, `index`, `cachedAt`
            ) VALUES (
                '$albumId', $connectionId, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入收藏专辑缓存测试数据。
     */
    private fun insertFavoriteAlbum(connection: SQLiteConnection, albumId: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `FavoriteAlbum` (
                `albumId`, `connectionId`, `ifFavorite`, `cachedAt`
            ) VALUES (
                '$albumId', $connectionId, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入艺术家专辑关系测试数据。
     */
    private fun insertArtistAlbum(
        connection: SQLiteConnection,
        artistId: String,
        albumId: String,
        connectionId: Long,
    ) {
        connection.execSQL(
            """
            INSERT INTO `ArtistAlbum` (
                `artistId`, `albumId`, `connectionId`, `index`, `cachedAt`
            ) VALUES (
                '$artistId', '$albumId', $connectionId, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入风格专辑关系测试数据。
     */
    private fun insertGenreAlbum(
        connection: SQLiteConnection,
        genreId: String,
        albumId: String,
        connectionId: Long,
    ) {
        connection.execSQL(
            """
            INSERT INTO `GenreAlbum` (
                `genreId`, `albumId`, `connectionId`, `index`, `cachedAt`
            ) VALUES (
                '$genreId', '$albumId', $connectionId, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入收藏艺术家缓存测试数据。
     */
    private fun insertFavoriteArtist(connection: SQLiteConnection, artistId: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `FavoriteArtist` (
                `artistId`, `connectionId`, `ifFavorite`, `cachedAt`
            ) VALUES (
                '$artistId', $connectionId, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入播放进度测试数据。
     */
    private fun insertProgress(
        connection: SQLiteConnection,
        musicId: String,
        albumId: String,
        connectionId: Long,
    ) {
        connection.execSQL(
            """
            INSERT INTO `progress` (
                `musicId`, `musicName`, `albumId`, `progress`, `progressPercentage`,
                `index`, `connectionId`, `createTime`
            ) VALUES (
                '$musicId', '$musicId-name', '$albumId', 10, 0.5,
                1, $connectionId, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入专辑播放历史开关测试数据。
     */
    private fun insertEnableProgress(connection: SQLiteConnection, albumId: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `xy_enable_progress` (
                `albumId`, `ifEnableAlbumHistory`, `connectionId`
            ) VALUES (
                '$albumId', 1, $connectionId
            )
            """.trimIndent()
        )
    }

    /**
     * 插入专辑跳过时间测试数据。
     */
    private fun insertSkipTime(connection: SQLiteConnection, id: Long, albumId: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `skip_time` (
                `id`, `albumId`, `headTime`, `endTime`, `connectionId`
            ) VALUES (
                $id, '$albumId', 1, 2, $connectionId
            )
            """.trimIndent()
        )
    }

    /**
     * 插入歌词偏移配置测试数据。
     */
    private fun insertLrcConfig(connection: SQLiteConnection, id: Long, itemId: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `xy_lrc_config` (
                `id`, `itemId`, `lrcOffsetMs`, `connectionId`
            ) VALUES (
                $id, '$itemId', 0, $connectionId
            )
            """.trimIndent()
        )
    }

    /**
     * 插入每日推荐历史测试数据。
     */
    private fun insertDailyRecommendHistory(connection: SQLiteConnection, songId: String, connectionId: Long) {
        connection.execSQL(
            """
            INSERT INTO `xy_daily_recommend_history` (
                `songId`, `connectionId`, `mediaLibraryId`, `recommendIndex`, `timestamp`
            ) VALUES (
                '$songId', $connectionId, 'library-1', 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入艺术家热门歌曲缓存测试数据。
     */
    private fun insertArtistPopularMusic(
        connection: SQLiteConnection,
        artistKey: String,
        musicId: String,
        connectionId: Long,
    ) {
        connection.execSQL(
            """
            INSERT INTO `ArtistPopularMusic` (
                `artistKey`, `musicId`, `connectionId`, `index`, `cachedAt`
            ) VALUES (
                '$artistKey', '$musicId', $connectionId, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 插入相似歌曲缓存测试数据。
     */
    private fun insertSimilarMusic(
        connection: SQLiteConnection,
        sourceMusicId: String,
        musicId: String,
        connectionId: Long,
    ) {
        connection.execSQL(
            """
            INSERT INTO `SimilarMusic` (
                `sourceMusicId`, `musicId`, `connectionId`, `index`, `cachedAt`
            ) VALUES (
                '$sourceMusicId', '$musicId', $connectionId, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * 统计指定表的行数，按需追加 WHERE 条件。
     */
    private fun countRows(connection: SQLiteConnection, tableName: String, whereClause: String? = null): Long {
        val sql = buildString {
            append("SELECT COUNT(*) FROM `")
            append(tableName)
            append("`")
            if (!whereClause.isNullOrBlank()) {
                append(" WHERE ")
                append(whereClause)
            }
        }
        return queryLong(connection, sql)
    }

    /**
     * 查询单个 Long 结果。
     */
    private fun queryLong(connection: SQLiteConnection, sql: String): Long {
        connection.prepare(sql).use { statement ->
            assertTrue(statement.step(), "查询应返回一行: $sql")
            return statement.getLong(0)
        }
    }

    /**
     * 校验 v10 到 v11 新增的查询性能索引都已经创建。
     */
    private fun assertPerformanceIndexesExist(connection: SQLiteConnection) {
        performanceIndexExpectations().forEach { (tableName, indexName) ->
            assertTrue(indexExists(connection, tableName, indexName), "$tableName 应存在索引 $indexName")
        }
    }

    /**
     * 返回本地库大列表和排序查询需要的新增索引名称。
     */
    private fun performanceIndexExpectations(): List<Pair<String, String>> {
        return listOf(
            "xy_music" to "index_xy_music_connectionId_year",
            "xy_music" to "index_xy_music_connectionId_lastPlayedDate",
            "xy_album" to "index_xy_album_connectionId_ifPlaylist_createTime",
            "HomeMusic" to "index_HomeMusic_connectionId_index_musicId",
            "FavoriteMusic" to "index_FavoriteMusic_connectionId_index_musicId",
            "FavoriteMusic" to "index_FavoriteMusic_connectionId_ifFavorite_musicId",
            "AlbumMusic" to "index_AlbumMusic_connectionId_index_musicId",
            "AlbumMusic" to "index_AlbumMusic_albumId_connectionId_index",
            "ArtistMusic" to "index_ArtistMusic_connectionId_index_musicId",
            "ArtistMusic" to "index_ArtistMusic_artistId_connectionId_index",
            "PlaylistMusic" to "index_PlaylistMusic_connectionId_index_playlistId",
            "PlaylistMusic" to "index_PlaylistMusic_playlistId_connectionId_index",
            "PlayHistoryMusic" to "index_PlayHistoryMusic_connectionId_index_musicId",
            "PlayQueueMusic" to "index_PlayQueueMusic_connectionId_index_musicId",
            "MaximumPlayMusic" to "index_MaximumPlayMusic_connectionId_index_musicId",
            "NewestMusic" to "index_NewestMusic_connectionId_index_musicId",
            "ArtistPopularMusic" to "index_ArtistPopularMusic_connectionId_artistKey_index_musicId",
            "SimilarMusic" to "index_SimilarMusic_connectionId_sourceMusicId_index_musicId",
            "HomeAlbum" to "index_HomeAlbum_connectionId_index_albumId",
            "NewestAlbum" to "index_NewestAlbum_connectionId_index_albumId",
            "PlayHistoryAlbum" to "index_PlayHistoryAlbum_connectionId_index_albumId",
            "MaximumPlayAlbum" to "index_MaximumPlayAlbum_connectionId_index_albumId",
            "ArtistAlbum" to "index_ArtistAlbum_connectionId_index_albumId",
            "ArtistAlbum" to "index_ArtistAlbum_artistId_connectionId_index",
            "GenreAlbum" to "index_GenreAlbum_genreId_connectionId_index",
            "progress" to "index_progress_albumId_createTime",
            "progress" to "index_progress_albumId_index",
            "xy_daily_recommend_history" to "index_xy_daily_recommend_history_connectionId_mediaLibraryId_timestamp_recommendIndex",
        )
    }

    /**
     * 通过 SQLite 元数据判断指定索引是否存在。
     */
    private fun indexExists(connection: SQLiteConnection, tableName: String, indexName: String): Boolean {
        connection.prepare("PRAGMA index_list(`$tableName`)").use { statement ->
            while (statement.step()) {
                if (statement.getText(1) == indexName) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 校验外键检查没有任何违规行。
     */
    private fun assertForeignKeyCheckIsEmpty(connection: SQLiteConnection) {
        val violations = mutableListOf<String>()
        connection.prepare("PRAGMA foreign_key_check").use { statement ->
            while (statement.step()) {
                val tableName = statement.getText(0)
                val rowId = statement.getLong(1)
                val parentTable = statement.getText(2)
                val foreignKeyId = statement.getLong(3)
                violations.add("$tableName:$rowId->$parentTable#$foreignKeyId")
            }
        }

        assertTrue(violations.isEmpty(), "foreign_key_check 应为空，实际为: $violations")
    }
}
