package cn.xybbz.config.module

import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.SettingsConfig
import cn.xybbz.localdata.config.DatabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ConnectionConfigModule {

    @Singleton
    @Provides
    fun connectionConfigServer(
        db: DatabaseClient,
        settingsConfig: SettingsConfig
    ): ConnectionConfigServer {
        val connectionConfigServer = ConnectionConfigServer(db, settingsConfig)
        return connectionConfigServer;
    }
}