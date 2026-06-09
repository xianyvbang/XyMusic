package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.PlaybackProgressReporter
import cn.xybbz.config.setting.SettingsManager
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class PlaybackProgressModule {

    @Single
    fun playbackProgressReporter(
        musicController: MusicCommonController,
        dataSourceManager: DataSourceManager,
        settingsManager: SettingsManager
    ): PlaybackProgressReporter {
        return PlaybackProgressReporter(musicController, dataSourceManager, settingsManager)
    }
}