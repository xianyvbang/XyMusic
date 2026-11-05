package cn.xybbz.di

import cn.xybbz.config.HomeDataRepository
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.music.PlayerEventCoordinator
import cn.xybbz.config.volume.VolumeServer
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.startup.PlayerInitializer
import cn.xybbz.startup.SessionCoordinator
import cn.xybbz.startup.StartupInitializer
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class StartupModule {

    /**
     * 提供轻量启动初始化器。
     * 该初始化器不依赖 DataSourceManager，避免应用启动阶段提前创建数据源链路。
     */
    @Singleton
    fun startupInitializer(
        homeDataRepository: HomeDataRepository,
        proxyConfigServer: ProxyConfigServer,
        settingsManager: SettingsManager,
        sessionCoordinator: SessionCoordinator,
    ): StartupInitializer {
        return StartupInitializer(
            homeDataRepository = homeDataRepository,
            proxyConfigServer = proxyConfigServer,
            settingsManager = settingsManager,
            sessionCoordinator = sessionCoordinator,
        )
    }

    /**
     * 提供统一的应用会话协调器。
     */
    @Singleton
    fun sessionCoordinator(
        dataSourceManager: DataSourceManager,
    ): SessionCoordinator {
        return SessionCoordinator(dataSourceManager)
    }

    /**
     * 提供独立的播放器初始化器，只有会话恢复后才会真正调用。
     */
    @Singleton
    fun playerInitializer(
        db: LocalDatabaseClient,
        downloadDb: DownloadDatabaseClient,
        musicController: MusicCommonController,
        musicPlayContext: MusicPlayContext,
        playerEventCoordinator: PlayerEventCoordinator,
        settingsManager: SettingsManager,
        volumeServer: VolumeServer,
    ): PlayerInitializer {
        return PlayerInitializer(
            db = db,
            downloadDb = downloadDb,
            musicController = musicController,
            musicPlayContext = musicPlayContext,
            playerEventCoordinator = playerEventCoordinator,
            settingsManager = settingsManager,
            volumeServer = volumeServer,
        )
    }
}
