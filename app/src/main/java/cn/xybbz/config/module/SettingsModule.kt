package cn.xybbz.config.module

import cn.xybbz.config.SettingsConfig
import cn.xybbz.localdata.config.DatabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class SettingsModule {

    @Singleton
    @Provides
    fun settingsConfig(db: DatabaseClient): SettingsConfig {
        val settingsConfig = SettingsConfig(db)
        settingsConfig.setSettingsData()
        return settingsConfig;
    }

}