package cn.xybbz.config.setting

import cn.xybbz.localdata.enums.LanguageType
import java.util.Locale

class JvmLanguagePlatformManager : LanguagePlatformManager {

    override fun applyLanguage(languageType: LanguageType) {
        Locale.setDefault(Locale.forLanguageTag(languageType.languageCode))
    }

    override fun getSystemLanguageType(): LanguageType {
        return LanguageType.getThis(Locale.getDefault().toLanguageTag()) ?: LanguageType.ZH_CN
    }
}
