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

package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.ui.components.JvmSettingGroup
import cn.xybbz.ui.components.JvmSettingGroupVerticalGap
import cn.xybbz.ui.components.JvmSettingPickRow
import cn.xybbz.viewmodel.LanguageConfigViewModel
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.jvm_language_config_screen_text_17
import xymusic.composeapp.generated.resources.jvm_language_config_screen_text_24
import xymusic.composeapp.generated.resources.jvm_language_config_screen_text_25
import xymusic.composeapp.generated.resources.jvm_language_config_screen_text_26
import xymusic.composeapp.generated.resources.jvm_language_config_screen_text_27
import xymusic.composeapp.generated.resources.language
import xymusic.composeapp.generated.resources.system

/**
 * 设置页内嵌的语言设置分区。
 *
 * 承接原本地化页的语言选择能力，外观完全复用设置页的分组标题与设置行样式：
 * 语言选项复用设置页单选行（与缓存上限档位一致）。
 *
 * 需要在设置页分组的宽度下调用，内部分组按设置页统一间距纵向排列。
 *
 * @param languageConfigViewModel 语言设置 ViewModel，负责读写当前语言。
 * @param modifier 外层布局修饰符，通常由设置页分组网格下发宽度。
 */
@Composable
internal fun JvmLanguageSettingsSection(
    languageConfigViewModel: LanguageConfigViewModel,
    modifier: Modifier = Modifier,
) {
    val settings by languageConfigViewModel.settingsManager.settings.collectAsState()
    // 已保存的固定语言；为空表示跟随系统。
    val configuredLanguageType = settings.languageType

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(JvmSettingGroupVerticalGap),
    ) {
        // 语言分组：跟随系统加各语言选项，复用设置页单选行样式。
        JvmSettingGroup(title = stringResource(Res.string.language)) {
            JvmSettingPickRow(
                title = stringResource(Res.string.system),
                description = stringResource(Res.string.jvm_language_config_screen_text_17),
                selected = configuredLanguageType == null,
                // 系统选项沿用本地化页的 AUTO 标识。
                tag = "AUTO",
                onClick = {
                    // 已是跟随系统时不再重复写入设置。
                    if (configuredLanguageType != null) {
                        languageConfigViewModel.updateLanguageType(null)
                    }
                },
            )

            LanguageType.entries.forEach { languageOption ->
                JvmSettingPickRow(
                    title = languageOption.displayName(),
                    description = languageOption.descriptionText(),
                    selected = configuredLanguageType == languageOption,
                    tag = languageOption.languageCode,
                    // 未开放的语言保持禁用观感，同时阻断切换。
                    enabled = languageOption.enabled,
                    onClick = {
                        if (languageOption.enabled && configuredLanguageType != languageOption) {
                            languageConfigViewModel.updateLanguageType(languageOption)
                        }
                    },
                )
            }
        }
    }
}

/** 语言枚举对应的显示名称。 */
@Composable
private fun LanguageType.displayName(): String {
    return when (this) {
        LanguageType.ZH_CN -> stringResource(Res.string.jvm_language_config_screen_text_24)
        LanguageType.EN -> "English"
        LanguageType.ZH_TW -> stringResource(Res.string.jvm_language_config_screen_text_25)
    }
}

/** 语言枚举对应的说明文案。 */
@Composable
private fun LanguageType.descriptionText(): String {
    return when (this) {
        LanguageType.ZH_CN -> stringResource(Res.string.jvm_language_config_screen_text_26)
        LanguageType.EN -> "English UI with English fallback strings."
        LanguageType.ZH_TW -> stringResource(Res.string.jvm_language_config_screen_text_27)
    }
}
