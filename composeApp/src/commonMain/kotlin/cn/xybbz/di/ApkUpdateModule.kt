package cn.xybbz.di

import cn.xybbz.api.client.version.VersionApiClient
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.update.ApkUpdateManager
import cn.xybbz.localdata.config.DatabaseClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class ApkUpdateModule {

    @Singleton
    fun apkUpdateManager(
        db: DatabaseClient,
        settingsManager: SettingsManager,
        versionApiClient: VersionApiClient
    ): ApkUpdateManager {
        return ApkUpdateManager(db, settingsManager, versionApiClient)
    }
}