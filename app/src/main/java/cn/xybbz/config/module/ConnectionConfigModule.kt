package cn.xybbz.config.module

import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.setting.SettingsManager
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
        settingsManager: SettingsManager
    ): ConnectionConfigServer {
        return ConnectionConfigServer(db, settingsManager)
    }
}