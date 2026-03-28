package cn.xybbz.di

import cn.xybbz.config.setting.LanguagePlatformManager
import org.koin.core.annotation.Module
import org.koin.core.scope.Scope

expect class LanguageModule() {
    fun languagePlatformManager(scope: Scope): LanguagePlatformManager
}
