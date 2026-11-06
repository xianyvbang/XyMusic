package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.music.PlaybackProgressReporter
import cn.xybbz.config.music.PlayerEventCoordinator
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.startup.SessionCoordinator
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.ui.state.PlayerChromeState
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
        sessionCoordinator: SessionCoordinator,
        settingsManager: SettingsManager,
        musicPlayContext: MusicPlayContext,
        playbackProgressReporter: PlaybackProgressReporter,
        playerChromeState: PlayerChromeState
    ): PlayerEventCoordinator {
        return PlayerEventCoordinator(
            db,
            musicController,
            dataSourceManager,
            sessionCoordinator,
            settingsManager,
            musicPlayContext,
            playbackProgressReporter,
            playerChromeState
        )
    }
}
