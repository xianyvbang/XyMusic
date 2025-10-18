package cn.xybbz.localdata.enums

enum class LanguageType(val languageCode: String,val languageName: String) {

    ZH_CN("zh-CN","简体中文"),
    EN("en","英文"),
    ZH_TW("zh-TW","繁体中文"),

    ;


    companion object {
        fun getThis(languageCode: String): LanguageType? {
            return entries.find { languageCode == it.languageCode  ||  languageCode.contains(it.languageCode)}
        }
    }
}