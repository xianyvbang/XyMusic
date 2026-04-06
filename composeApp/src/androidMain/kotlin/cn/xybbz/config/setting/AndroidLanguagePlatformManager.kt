package cn.xybbz.config.setting

import cn.xybbz.platform.ContextWrapper
import cn.xybbz.localdata.enums.LanguageType
import com.hjq.language.MultiLanguages
import java.util.Locale

class AndroidLanguagePlatformManager(
    private val contextWrapper: ContextWrapper
) : LanguagePlatformManager {

    override fun applyLanguage(languageType: LanguageType) {
        val locale = Locale.forLanguageTag(languageType.languageCode)
        MultiLanguages.setAppLanguage(contextWrapper.context, locale)
    }

    override fun getSystemLanguageType(): LanguageType {
        val systemLanguage = MultiLanguages.getSystemLanguage(contextWrapper.context)
        return LanguageType.getThis(systemLanguage.toLanguageTag()) ?: LanguageType.ZH_CN
    }
}
