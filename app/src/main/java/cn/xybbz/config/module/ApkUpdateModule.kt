package cn.xybbz.config.module

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import cn.xybbz.api.client.version.VersionApiClient
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.update.ApkUpdateManager
import cn.xybbz.localdata.config.DatabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApkUpdateModule {

    @OptIn(UnstableApi::class)
    @Singleton
    @Provides
    fun apkUpdateManager(
        db: DatabaseClient,
        settingsManager: SettingsManager,
        versionApiClient: VersionApiClient
    ): ApkUpdateManager {
        return ApkUpdateManager(db, settingsManager, versionApiClient)
    }
}