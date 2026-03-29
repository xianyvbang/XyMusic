package cn.xybbz.di

import cn.xybbz.config.setting.AndroidLanguagePlatformManager
import cn.xybbz.config.setting.LanguagePlatformManager
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
actual class LanguageModule actual constructor() {

    @Singleton
    actual fun languagePlatformManager(contextWrapper: ContextWrapper): LanguagePlatformManager {
        return AndroidLanguagePlatformManager(contextWrapper)
    }
}
