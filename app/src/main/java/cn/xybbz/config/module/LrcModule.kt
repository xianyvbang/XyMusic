package cn.xybbz.config.module

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.connection.ConnectionConfigServer
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
        ataSourceManager: DataSourceManager,
        db: DatabaseClient,
        connectionConfigServer: ConnectionConfigServer
    ): LrcServer {
        val lrcServer = LrcServer(musicController, ataSourceManager, db, connectionConfigServer)
        return lrcServer
    }
}