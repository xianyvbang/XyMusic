package cn.xybbz.config.module

import android.content.Context
import cn.xybbz.config.setting.SettingsManager
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
    fun settingsManager(
        db: DatabaseClient,
        @ApplicationContext applicationContext: Context
    ): SettingsManager {
        val settingsManager = SettingsManager(db, applicationContext)
        settingsManager.setSettingsData()
        return settingsManager;
    }
}