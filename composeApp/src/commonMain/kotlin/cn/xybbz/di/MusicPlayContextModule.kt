package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.localdata.config.DatabaseClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class MusicPlayContextModule {

    @Singleton
    fun musicPlayContext(
        dataSourceManager: DataSourceManager,
        musicController: MusicCommonController,
        db: DatabaseClient
    ): MusicPlayContext {
        return MusicPlayContext(
            dataSourceManager = dataSourceManager,
            musicController = musicController,
            db = db
        )
    }
}
