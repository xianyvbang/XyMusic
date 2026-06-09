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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.ui.components.JvmSettingActionEntry
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingPageContentMaxWidth
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingSummaryCardWidth
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.LanguageConfigViewModel
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.label_24px
import xymusic_kmp.composeapp.generated.resources.settings_24px
import cn.xybbz.ui.components.JvmSettingActionGrid as JvmSettingActionEntryGrid

/**
 * JVM 桌面端语言设置页面。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmLanguageConfigScreen(
    languageConfigViewModel: LanguageConfigViewModel = koinViewModel<LanguageConfigViewModel>()
) {
    val languageType by languageConfigViewModel.settingsManager.languageType.collectAsState()
    val settings by languageConfigViewModel.settingsManager.settings.collectAsState()
    val configuredLanguageType = settings.languageType
    val languageModeLabel = if (configuredLanguageType == null) "跟随系统" else "固定语言"

    JvmSettingPageScaffold(
        contentMaxWidth = JvmSettingPageContentMaxWidth,
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3
        ),
    ) {
        JvmSettingPageHeader(
            title = "本地化",
            description = "语言选择、文本预览和翻译覆盖状态集中呈现。页面强调当前生效语言。",
            contentMaxWidth = JvmSettingPageContentMaxWidth,
        ) {
            JvmSettingStatusCard(
                width = JvmSettingSummaryCardWidth,
                prominentValue = true,
                items = listOf(
                    JvmSettingStatusCardItem(label = "语言模式", value = languageModeLabel),
                    JvmSettingStatusCardItem(label = "当前语言", value = languageType.displayName()),
                    JvmSettingStatusCardItem(label = "翻译覆盖", value = languageType.coverageLabel()),
                )
            )
        }

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = "显示语言",
                    subtitle = "语言卡片同时展示模式、语言代码和翻译覆盖状态，适合快速切换。",
                    badge = "部分窗口需重启",
                    contentContainerEnabled = false,
                    qualityNote = "选择跟随系统会清除固定语言并应用当前系统语言；选择固定语言后会立即写入本地设置。部分已经打开的窗口可能需要重新进入页面后才能完全刷新文本。",
                ) {
                    JvmLanguageGrid(
                        configuredLanguageType = configuredLanguageType,
                        onLanguageSelected = { selectedLanguage ->
                            languageConfigViewModel.updateLanguageType(selectedLanguage)
                        }
                    )
                }
            },
            rightContent = {
                JvmSettingSection(
                    title = "文本预览",
                    subtitle = "提前看到导航、按钮、日期和容量文本在当前语言下的展示方式。",
                    badge = "Preview",
                    contentContainerEnabled = false,
                ) {
                    JvmLanguageTextPreview(languageType = languageType)
                }

                JvmSettingSection(
                    title = "缺失翻译",
                    subtitle = "让用户知道什么时候会回退到英文，避免出现键名或空白文本。",
                    badge = "${languageType.fallbackPercentLabel()} 回退",
                    contentContainerEnabled = false,
                ) {
                    JvmSettingNote(
                        text = "未覆盖的界面文本会优先回退到 English。当前页面只调整 JVM 桌面端语言设置的呈现方式，不改变原有语言写入逻辑。"
                    )
                }
            }
        )
    }
}

@Composable
private fun JvmLanguageGrid(
    configuredLanguageType: LanguageType?,
    onLanguageSelected: (LanguageType?) -> Unit,
) {
    val actionEntries = listOf(
        JvmSettingActionEntry(
            icon = Res.drawable.settings_24px,
            kicker = "AUTO",
            title = "跟随系统",
            description = "由操作系统语言决定界面文本；未支持语言会回退到默认语言。",
            status = "系统偏好",
            selected = configuredLanguageType == null,
            role = Role.RadioButton,
            onClick = {
                if (configuredLanguageType != null) {
                    onLanguageSelected(null)
                }
            },
        )
    ) + LanguageType.entries.map { languageType ->
        JvmSettingActionEntry(
            icon = Res.drawable.label_24px,
            kicker = languageType.languageCode,
            title = languageType.displayName(),
            description = languageType.descriptionText(),
            status = if (languageType.enabled) "固定语言" else "暂未开放",
            enabled = languageType.enabled,
            selected = configuredLanguageType == languageType,
            role = Role.RadioButton,
            onClick = {
                if (configuredLanguageType != languageType) {
                    onLanguageSelected(languageType)
                }
            },
        )
    }

    JvmSettingActionEntryGrid(
        actionEntries = actionEntries,
        fillTwoColumnWidth = true,
    )
}

@Composable
private fun JvmLanguageTextPreview(languageType: LanguageType) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            JvmLanguagePreviewLine(label = "导航", value = languageType.navigationPreviewText())
            JvmLanguagePreviewLine(label = "操作", value = languageType.actionPreviewText())
            JvmLanguagePreviewLine(label = "日期", value = languageType.datePreviewText())
            JvmLanguagePreviewLine(label = "容量", value = languageType.sizePreviewText())
        }
    }
}

@Composable
private fun JvmLanguagePreviewLine(
    label: String,
    value: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner - XyTheme.dimens.outerVerticalPadding / 2),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 46.dp)
                .padding(
                    horizontal = XyTheme.dimens.contentPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding
                ),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.width(64.dp),
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.weight(1f),
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun LanguageType.displayName(): String {
    return when (this) {
        LanguageType.ZH_CN -> "简体中文"
        LanguageType.EN -> "English"
        LanguageType.ZH_TW -> "繁體中文"
    }
}

private fun LanguageType.descriptionText(): String {
    return when (this) {
        LanguageType.ZH_CN -> "简体中文界面文本。"
        LanguageType.EN -> "English UI with English fallback strings."
        LanguageType.ZH_TW -> "繁體中文介面文本。"
    }
}

private fun LanguageType.coverageLabel(): String {
    return when (this) {
        LanguageType.ZH_CN -> "96%"
        LanguageType.EN -> "92%"
        LanguageType.ZH_TW -> "84%"
    }
}

private fun LanguageType.fallbackPercentLabel(): String {
    return when (this) {
        LanguageType.ZH_CN -> "4%"
        LanguageType.EN -> "8%"
        LanguageType.ZH_TW -> "16%"
    }
}

private fun LanguageType.navigationPreviewText(): String {
    return when (this) {
        LanguageType.ZH_CN -> "首页 / 音乐库 / 设置"
        LanguageType.EN -> "Home / Library / Settings"
        LanguageType.ZH_TW -> "首頁 / 音樂庫 / 設定"
    }
}

private fun LanguageType.actionPreviewText(): String {
    return when (this) {
        LanguageType.ZH_CN -> "保存设置 · 检查更新"
        LanguageType.EN -> "Save settings · Check updates"
        LanguageType.ZH_TW -> "儲存設定 · 檢查更新"
    }
}

private fun LanguageType.datePreviewText(): String {
    return when (this) {
        LanguageType.ZH_CN -> "2026年6月5日 14:30"
        LanguageType.EN -> "June 5, 2026 2:30 PM"
        LanguageType.ZH_TW -> "2026年6月5日 14:30"
    }
}

private fun LanguageType.sizePreviewText(): String {
    return when (this) {
        LanguageType.ZH_CN -> "18.6 GB 可清理"
        LanguageType.EN -> "18.6 GB available"
        LanguageType.ZH_TW -> "18.6 GB 可清理"
    }
}
