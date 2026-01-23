package cn.xybbz.config.module

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.localdata.config.DatabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LrcModule {

    @Singleton
    @Provides
    fun lrcServer(
        musicController: MusicController,
        dataSourceManager: DataSourceManager,
        db: DatabaseClient,
    ): LrcServer {
        val lrcServer = LrcServer(musicController, dataSourceManager, db,)
        return lrcServer
    }
}