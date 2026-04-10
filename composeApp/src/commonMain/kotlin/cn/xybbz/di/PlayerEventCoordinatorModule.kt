package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.music.PlaybackProgressReporter
import cn.xybbz.config.music.PlayerEventCoordinator
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.config.LocalDatabaseClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class PlayerEventCoordinatorModule {

    @Single
    fun providePlayerEventCoordinator(
        db: LocalDatabaseClient,
        musicController: MusicCommonController,
        dataSourceManager: DataSourceManager,
        settingsManager: SettingsManager,
        musicPlayContext: MusicPlayContext,
        playbackProgressReporter: PlaybackProgressReporter
    ): PlayerEventCoordinator {
        return PlayerEventCoordinator(
            db,
            musicController,
            dataSourceManager,
            settingsManager,
            musicPlayContext,
            playbackProgressReporter
        )
    }
}