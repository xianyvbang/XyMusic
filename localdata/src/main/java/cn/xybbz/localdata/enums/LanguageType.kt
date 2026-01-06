/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.localdata.enums

enum class LanguageType(val languageCode: String,val languageName: String,val enabled: Boolean = true) {

    ZH_CN("zh-CN","简体中文"),
    EN("en","英文",false),
    ZH_TW("zh-TW","繁体中文"),

    ;


    companion object {
        fun getThis(languageCode: String): LanguageType? {
            return entries.find { languageCode == it.languageCode  ||  languageCode.contains(it.languageCode)}
        }
    }
}