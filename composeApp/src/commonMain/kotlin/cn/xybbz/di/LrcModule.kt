package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.client.custom.CustomMediaApiClient
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.config.DatabaseClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class LrcModule {

    @Singleton
    fun lrcServer(
        musicController: MusicCommonController,
        dataSourceManager: DataSourceManager,
        db: DatabaseClient,
        settingsManager: SettingsManager,
        customMediaApiClient: CustomMediaApiClient
    ): LrcServer {
        val lrcServer = LrcServer(
            musicController,
            dataSourceManager,
            db,
            settingsManager,
            customMediaApiClient
        )
        return lrcServer
    }
}
