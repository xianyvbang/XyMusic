package cn.xybbz.di

import cn.xybbz.config.setting.LanguagePlatformManager
import org.koin.core.annotation.Module

expect class LanguageModule() {
    fun languagePlatformManager(contextWrapper: ContextWrapper): LanguagePlatformManager
}
