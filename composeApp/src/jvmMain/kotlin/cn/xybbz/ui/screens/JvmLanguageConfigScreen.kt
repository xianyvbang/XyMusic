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
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingSummaryCardWidth
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.LanguageConfigViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.*
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
    val languageModeLabel = if (configuredLanguageType == null) stringResource(Res.string.system) else stringResource(Res.string.jvm_language_config_screen_text_01)

    JvmSettingPageScaffold(
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3
        ),
    ) {
        JvmSettingPageHeader(
            title = stringResource(Res.string.jvm_language_config_screen_text_02),
            description = stringResource(Res.string.jvm_language_config_screen_text_03),
        ) {
            JvmSettingStatusCard(
                width = JvmSettingSummaryCardWidth,
                prominentValue = true,
                items = listOf(
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_language_config_screen_text_04), value = languageModeLabel),
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_language_config_screen_text_05), value = languageType.displayName()),
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_language_config_screen_text_06), value = languageType.coverageLabel()),
                )
            )
        }

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = stringResource(Res.string.jvm_language_config_screen_text_07),
                    subtitle = stringResource(Res.string.jvm_language_config_screen_text_08),
                    badge = stringResource(Res.string.jvm_language_config_screen_text_09),
                    contentContainerEnabled = false,
                    qualityNote = stringResource(Res.string.jvm_language_config_screen_text_10),
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
                    title = stringResource(Res.string.jvm_language_config_screen_text_11),
                    subtitle = stringResource(Res.string.jvm_language_config_screen_text_12),
                    badge = "Preview",
                    contentContainerEnabled = false,
                ) {
                    JvmLanguageTextPreview(languageType = languageType)
                }

                JvmSettingSection(
                    title = stringResource(Res.string.jvm_language_config_screen_text_13),
                    subtitle = stringResource(Res.string.jvm_language_config_screen_text_14),
                    badge = stringResource(Res.string.jvm_language_config_screen_text_15, languageType.fallbackPercentLabel()),
                    contentContainerEnabled = false,
                ) {
                    JvmSettingNote(
                        text = stringResource(Res.string.jvm_language_config_screen_text_16)
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
            title = stringResource(Res.string.system),
            description = stringResource(Res.string.jvm_language_config_screen_text_17),
            status = stringResource(Res.string.jvm_language_config_screen_text_18),
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
            status = if (languageType.enabled) stringResource(Res.string.jvm_language_config_screen_text_01) else stringResource(Res.string.jvm_language_config_screen_text_19),
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
            JvmLanguagePreviewLine(label = stringResource(Res.string.jvm_language_config_screen_text_20), value = languageType.navigationPreviewText())
            JvmLanguagePreviewLine(label = stringResource(Res.string.jvm_language_config_screen_text_21), value = languageType.actionPreviewText())
            JvmLanguagePreviewLine(label = stringResource(Res.string.jvm_language_config_screen_text_22), value = languageType.datePreviewText())
            JvmLanguagePreviewLine(label = stringResource(Res.string.jvm_language_config_screen_text_23), value = languageType.sizePreviewText())
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

@Composable
private fun LanguageType.displayName(): String {
    return when (this) {
        LanguageType.ZH_CN -> stringResource(Res.string.jvm_language_config_screen_text_24)
        LanguageType.EN -> "English"
        LanguageType.ZH_TW -> stringResource(Res.string.jvm_language_config_screen_text_25)
    }
}

@Composable
private fun LanguageType.descriptionText(): String {
    return when (this) {
        LanguageType.ZH_CN -> stringResource(Res.string.jvm_language_config_screen_text_26)
        LanguageType.EN -> "English UI with English fallback strings."
        LanguageType.ZH_TW -> stringResource(Res.string.jvm_language_config_screen_text_27)
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

@Composable
private fun LanguageType.navigationPreviewText(): String {
    return when (this) {
        LanguageType.ZH_CN -> stringResource(Res.string.jvm_language_config_screen_text_28)
        LanguageType.EN -> "Home / Library / Settings"
        LanguageType.ZH_TW -> stringResource(Res.string.jvm_language_config_screen_text_29)
    }
}

@Composable
private fun LanguageType.actionPreviewText(): String {
    return when (this) {
        LanguageType.ZH_CN -> stringResource(Res.string.jvm_language_config_screen_text_30)
        LanguageType.EN -> "Save settings · Check updates"
        LanguageType.ZH_TW -> stringResource(Res.string.jvm_language_config_screen_text_31)
    }
}

@Composable
private fun LanguageType.datePreviewText(): String {
    return when (this) {
        LanguageType.ZH_CN -> stringResource(Res.string.jvm_language_config_screen_text_32)
        LanguageType.EN -> "June 5, 2026 2:30 PM"
        LanguageType.ZH_TW -> stringResource(Res.string.jvm_language_config_screen_text_32)
    }
}

@Composable
private fun LanguageType.sizePreviewText(): String {
    return when (this) {
        LanguageType.ZH_CN -> stringResource(Res.string.jvm_language_config_screen_text_33)
        LanguageType.EN -> "18.6 GB available"
        LanguageType.ZH_TW -> stringResource(Res.string.jvm_language_config_screen_text_33)
    }
}
