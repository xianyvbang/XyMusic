package cn.xybbz.di

import cn.xybbz.config.setting.AndroidLanguagePlatformManager
import cn.xybbz.config.setting.LanguagePlatformManager
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.core.scope.Scope
import org.koin.core.scope.get

@Module
@Configuration
actual class LanguageModule actual constructor() {

    @Singleton
    actual fun languagePlatformManager(scope: Scope): LanguagePlatformManager {
        return AndroidLanguagePlatformManager(scope.get<ContextWrapper>())
    }
}
