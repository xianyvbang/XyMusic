package cn.xybbz.config.setting

import cn.xybbz.localdata.enums.LanguageType
import platform.Foundation.NSLocale

class IosLanguagePlatformManager : LanguagePlatformManager {

    override fun applyLanguage(languageType: LanguageType) {
        // Compose Multiplatform on iOS does not have a unified runtime language switch here yet.
    }

    override fun getSystemLanguageType(): LanguageType {
        val localeIdentifier = NSLocale.currentLocale.localeIdentifier.replace("_", "-")
        return LanguageType.getThis(localeIdentifier) ?: LanguageType.ZH_CN
    }
}
