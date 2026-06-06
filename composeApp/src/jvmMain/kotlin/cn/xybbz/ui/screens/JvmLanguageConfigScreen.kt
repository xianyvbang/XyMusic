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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.ui.components.JvmSettingFlowRow
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingPageContentMaxWidth
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.LanguageConfigViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.label_24px
import xymusic_kmp.composeapp.generated.resources.settings_24px

private val JvmLanguageSummaryWidth = 278.dp
private val JvmLanguageGridTwoColumnMinWidth = 320.dp
private val JvmLanguageCardCompactWidth = 154.dp
private val JvmLanguageCardHeight = 148.dp
private val JvmLanguageCardLiftOffset = (-6).dp

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
                width = JvmLanguageSummaryWidth,
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
    val options = listOf(
        JvmLanguageOption(
            type = null,
            icon = Res.drawable.settings_24px,
            code = "AUTO",
            title = "跟随系统",
            description = "由操作系统语言决定界面文本；未支持语言会回退到默认语言。",
            status = "系统偏好",
        )
    ) + LanguageType.entries.map { languageType ->
        JvmLanguageOption(
            type = languageType,
            icon = Res.drawable.label_24px,
            code = languageType.languageCode,
            title = languageType.displayName(),
            description = languageType.descriptionText(),
            status = if (languageType.enabled) "固定语言" else "暂未开放",
            enabled = languageType.enabled,
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding
        val twoColumnMinWidth = maxOf(
            JvmLanguageGridTwoColumnMinWidth,
            JvmLanguageCardCompactWidth * 2f + gap
        )
        val columnCount = if (maxWidth >= twoColumnMinWidth) 2 else 1
        val cardWidth = if (columnCount == 2) {
            (maxWidth - gap) / 2f
        } else {
            maxWidth
        }

        JvmSettingFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            options.forEach { option ->
                val selected = configuredLanguageType == option.type
                JvmLanguageCard(
                    modifier = Modifier.width(cardWidth),
                    option = option,
                    selected = selected,
                    onClick = {
                        if (option.enabled && !selected) {
                            onLanguageSelected(option.type)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun JvmLanguageCard(
    option: JvmLanguageOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = RoundedCornerShape(XyTheme.dimens.corner)
    val colorScheme = MaterialTheme.colorScheme
    val enabled = option.enabled
    val cardHovered = enabled && hovered
    val contentAlpha = if (enabled) 1f else 0.44f
    val clickableModifier = if (enabled) {
        Modifier.jvmHoverDebounceClickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.RadioButton,
            onClick = onClick
        )
    } else {
        Modifier
    }
    val liftOffset by animateDpAsState(
        targetValue = if (cardHovered) JvmLanguageCardLiftOffset else 0.dp,
        animationSpec = tween(durationMillis = 160),
        label = "language_card_lift_offset",
    )
    val containerColor = if (selected) {
        colorScheme.primary.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.18f else 0.10f)
    } else {
        colorScheme.surfaceContainerLowest
    }
    val borderColor = if (selected) {
        colorScheme.primary.copy(alpha = 0.72f)
    } else {
        colorScheme.onSurface.copy(alpha = 0.10f)
    }

    Box(
        modifier = modifier
            .heightIn(
                min = JvmLanguageCardHeight,
                max = JvmLanguageCardHeight
            )
            .then(clickableModifier)
    ) {
        Column(
            modifier = Modifier
                .offset(y = liftOffset)
                .fillMaxSize()
                .clip(shape)
                .background(containerColor)
                .border(BorderStroke(1.dp, borderColor), shape)
                .padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            JvmLanguageKicker(
                icon = option.icon,
                text = option.code,
                contentAlpha = contentAlpha,
            )
            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurface.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = option.description,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = option.status,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurfaceVariant.copy(alpha = contentAlpha * 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
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

@Composable
private fun JvmLanguageKicker(
    icon: DrawableResource,
    text: String,
    contentAlpha: Float,
) {
    val shape = RoundedCornerShape(XyTheme.dimens.corner - XyTheme.dimens.outerVerticalPadding / 2)
    Row(
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f * contentAlpha),
                    shape = shape
                )
                .border(
                    BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f * contentAlpha)
                    ),
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private data class JvmLanguageOption(
    val type: LanguageType?,
    val icon: DrawableResource,
    val code: String,
    val title: String,
    val description: String,
    val status: String,
    val enabled: Boolean = true,
)

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
