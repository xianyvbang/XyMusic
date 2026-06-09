package cn.xybbz.config.setting

import cn.xybbz.localdata.enums.LanguageType

interface LanguagePlatformManager {
    fun applyLanguage(languageType: LanguageType)

    fun getSystemLanguageType(): LanguageType
}
