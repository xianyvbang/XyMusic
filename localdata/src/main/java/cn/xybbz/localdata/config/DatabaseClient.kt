package cn.xybbz.localdata.config

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cn.xybbz.localdata.converter.XyMusicTypeConverter
import cn.xybbz.localdata.dao.album.AlbumDao
import cn.xybbz.localdata.dao.connection.ConnectionConfigDao
import cn.xybbz.localdata.dao.count.XyDataCountDao
import cn.xybbz.localdata.dao.download.XyDownloadDao
import cn.xybbz.localdata.dao.era.XyEraItemDao
import cn.xybbz.localdata.dao.genre.XyGenreDao
import cn.xybbz.localdata.dao.library.XyLibraryDao
import cn.xybbz.localdata.dao.music.ArtistDao
import cn.xybbz.localdata.dao.music.XyMusicDao
import cn.xybbz.localdata.dao.player.XyPlayerDao
import cn.xybbz.localdata.dao.progress.EnableProgressDao
import cn.xybbz.localdata.dao.progress.ProgressDao
import cn.xybbz.localdata.dao.proxy.XyProxyConfigDao
import cn.xybbz.localdata.dao.recommend.XyRecentHistoryDao
import cn.xybbz.localdata.dao.remote.RemoteCurrentDao
import cn.xybbz.localdata.dao.search.SearchHistoryDao
import cn.xybbz.localdata.dao.setting.SettingsDao
import cn.xybbz.localdata.dao.setting.SkipTimeDao
import cn.xybbz.localdata.dao.setting.XyBackgroundConfigDao
import cn.xybbz.localdata.data.album.ArtistAlbum
import cn.xybbz.localdata.data.album.FavoriteAlbum
import cn.xybbz.localdata.data.album.GenreAlbum
import cn.xybbz.localdata.data.album.HomeAlbum
import cn.xybbz.localdata.data.album.MaximumPlayAlbum
import cn.xybbz.localdata.data.album.NewestAlbum
import cn.xybbz.localdata.data.album.PlayHistoryAlbum
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.FavoriteArtist
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.count.XyDataCount
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.data.era.XyEraItem
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.library.XyLibrary
import cn.xybbz.localdata.data.music.AlbumMusic
import cn.xybbz.localdata.data.music.ArtistMusic
import cn.xybbz.localdata.data.music.FavoriteMusic
import cn.xybbz.localdata.data.music.HomeMusic
import cn.xybbz.localdata.data.music.MaximumPlayMusic
import cn.xybbz.localdata.data.music.NewestMusic
import cn.xybbz.localdata.data.music.PlayHistoryMusic
import cn.xybbz.localdata.data.music.PlayQueueMusic
import cn.xybbz.localdata.data.music.PlaylistMusic
import cn.xybbz.localdata.data.music.RecommendedMusic
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.player.XyPlayer
import cn.xybbz.localdata.data.progress.EnableProgress
import cn.xybbz.localdata.data.progress.Progress
import cn.xybbz.localdata.data.proxy.XyProxyConfig
import cn.xybbz.localdata.data.recommend.XyRecentHistory
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.data.search.SearchHistory
import cn.xybbz.localdata.data.setting.SkipTime
import cn.xybbz.localdata.data.setting.XyBackgroundConfig
import cn.xybbz.localdata.data.setting.XySettings

@Database(
    version = 12,
    entities = [XyMusic::class, XyAlbum::class, XySettings::class, SkipTime::class,
        RemoteCurrent::class, SearchHistory::class, Progress::class, XyArtist::class,
        EnableProgress::class, XyLibrary::class, XyPlayer::class, ConnectionConfig::class,
        XyEraItem::class, XyGenre::class, AlbumMusic::class, ArtistAlbum::class, ArtistMusic::class,
        FavoriteMusic::class, GenreAlbum::class, HomeAlbum::class, HomeMusic::class,
        MaximumPlayMusic::class, NewestAlbum::class, NewestMusic::class, PlayHistoryMusic::class,
        PlaylistMusic::class, PlayQueueMusic::class, XyDataCount::class, PlayHistoryAlbum::class,
        MaximumPlayAlbum::class, FavoriteAlbum::class, FavoriteArtist::class, XyBackgroundConfig::class,
        XyRecentHistory::class, RecommendedMusic::class, XyDownload::class, XyProxyConfig::class],
    exportSchema = true
)
@TypeConverters(XyMusicTypeConverter::class)
abstract class DatabaseClient : RoomDatabase() {

    val musicDao: XyMusicDao by lazy { createMusicDao() }

    val albumDao: AlbumDao by lazy { createAlbumDao() }

    val settingsDao: SettingsDao by lazy { createSettingsDao() }

    val skipTimeDao: SkipTimeDao by lazy { createSkipTimeDao() }

    val remoteCurrentDao: RemoteCurrentDao by lazy { createRemoteCurrentDao() }

    val searchHistoryDao: SearchHistoryDao by lazy { createSearchHistoryDao() }

    val progressDao: ProgressDao by lazy { createProgressDao() }

    val artistDao: ArtistDao by lazy { createArtistItemDao() }

    val enableProgressDao: EnableProgressDao by lazy { createEnableProgressDao() }

    val playerDao: XyPlayerDao by lazy { createPlayerDao() }

    val connectionConfigDao: ConnectionConfigDao by lazy { createConnectionConfigDao() }

    val eraItemDao: XyEraItemDao by lazy { createEraItemDao() }

    val libraryDao: XyLibraryDao by lazy { createLibraryDao() }

    val genreDao: XyGenreDao by lazy { createXyGenreDao() }

    val dataCountDao: XyDataCountDao by lazy { createXyDataCountDao() }

    val backgroundConfigDao: XyBackgroundConfigDao by lazy { createXyBackgroundConfigDao() }

    val recentHistoryDao: XyRecentHistoryDao by lazy { createXyRecentHistoryDao() }

    val downloadDao: XyDownloadDao by lazy { createXyDownloadDao() }

    val proxyConfigDao: XyProxyConfigDao by lazy { createXyProxyConfigDao() }


    abstract fun createMusicDao(): XyMusicDao

    abstract fun createAlbumDao(): AlbumDao

    abstract fun createSettingsDao(): SettingsDao

    abstract fun createSkipTimeDao(): SkipTimeDao

    abstract fun createRemoteCurrentDao(): RemoteCurrentDao

    abstract fun createSearchHistoryDao(): SearchHistoryDao

    abstract fun createProgressDao(): ProgressDao

    abstract fun createArtistItemDao(): ArtistDao

    abstract fun createEnableProgressDao(): EnableProgressDao

    abstract fun createPlayerDao(): XyPlayerDao

    abstract fun createConnectionConfigDao(): ConnectionConfigDao

    abstract fun createEraItemDao(): XyEraItemDao

    abstract fun createLibraryDao(): XyLibraryDao

    abstract fun createXyGenreDao(): XyGenreDao

    abstract fun createXyDataCountDao(): XyDataCountDao

    abstract fun createXyBackgroundConfigDao(): XyBackgroundConfigDao

    abstract fun createXyRecentHistoryDao(): XyRecentHistoryDao

    abstract fun createXyDownloadDao(): XyDownloadDao

    abstract fun createXyProxyConfigDao(): XyProxyConfigDao

}