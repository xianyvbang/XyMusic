package cn.xybbz.di

import cn.xybbz.config.music.DownloadCacheCommonController
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.music.DownloadCacheController
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
actual class CacheModule {
    @Singleton
    actual fun downloadCacheController(
        contextWrapper: ContextWrapper,
        settingsManager: SettingsManager
    ): DownloadCacheCommonController {
        return DownloadCacheController(contextWrapper.context, settingsManager)
    }
}