package cn.xybbz.config.module

import android.content.Context
import cn.xybbz.config.SettingsConfig
import cn.xybbz.localdata.config.DatabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class SettingsModule {

    @Singleton
    @Provides
    fun settingsConfig(
        db: DatabaseClient,
        @ApplicationContext applicationContext: Context
    ): SettingsConfig {
        val settingsConfig = SettingsConfig(db, applicationContext)
        settingsConfig.setSettingsData()
        return settingsConfig;
    }
}