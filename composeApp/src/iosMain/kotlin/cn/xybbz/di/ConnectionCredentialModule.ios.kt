package cn.xybbz.di

import cn.xybbz.config.security.IosCredentialSyncPolicy
import cn.xybbz.config.security.IosCredentialSyncSettingsProvider
import cn.xybbz.config.security.SettingsManagerIosCredentialSyncSettingsProvider
import cn.xybbz.config.setting.SettingsManager
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

/**
 * iOS 连接凭据同步依赖模块。
 */
@Module
@Configuration
class ConnectionCredentialModuleIos {
    /**
     * 提供 iOS Keychain 同步设置读取器。
     */
    @Singleton
    fun iosCredentialSyncSettingsProvider(
        settingsManager: SettingsManager,
    ): IosCredentialSyncSettingsProvider {
        return SettingsManagerIosCredentialSyncSettingsProvider(settingsManager)
    }

    /**
     * 提供 iOS Keychain 同步策略。
     */
    @Singleton
    fun iosCredentialSyncPolicy(
        settingsRepository: IosCredentialSyncSettingsProvider,
    ): IosCredentialSyncPolicy {
        return IosCredentialSyncPolicy(settingsRepository)
    }
}
