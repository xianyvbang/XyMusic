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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.common.enums.toResStringInt
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.router.SetBackgroundImage
import cn.xybbz.ui.components.JvmSettingActionEntry
import cn.xybbz.ui.components.JvmSettingFlowRow
import cn.xybbz.ui.components.JvmSettingNavigationRow
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingInterfaceMiniPreviewHeight
import cn.xybbz.ui.components.JvmSettingSummaryCardWidth
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.InterfaceSettingViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album_24px
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.background_image_setting
import xymusic_kmp.composeapp.generated.resources.interface_settings
import xymusic_kmp.composeapp.generated.resources.return_setting_screen
import xymusic_kmp.composeapp.generated.resources.settings_24px
import cn.xybbz.ui.components.JvmSettingActionGrid as JvmSettingActionEntryGrid
import cn.xybbz.ui.xy.XyIconButton as IconButton

/**
 * 界面设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmInterfaceSettingScreen(
    showBackButton: Boolean = true,
    interfaceSettingViewModel: InterfaceSettingViewModel = koinViewModel<InterfaceSettingViewModel>()
) {
    val navigator = LocalNavigator.current
    val themeType by interfaceSettingViewModel.settingsManager.themeType.collectAsState()
    val imageFilePath by interfaceSettingViewModel.settingsManager.imageFilePath.collectAsState()
    val themeLabel = stringResource(themeType.toResStringInt())
    val backgroundConfigured = !imageFilePath.isNullOrBlank()
    val backgroundStatus = if (backgroundConfigured) "已设置" else "未设置"
    val interfaceSettingsTitle = stringResource(Res.string.interface_settings)
    val backgroundImageTitle = stringResource(Res.string.background_image_setting)
    val topBar: (@Composable () -> Unit)? = if (showBackButton) {
        {
            TopAppBarComponent(
                title = {
                    TopAppBarTitle(title = interfaceSettingsTitle)
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigator.goBack()
                        },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.return_setting_screen)
                        )
                    }
                }
            )
        }
    } else {
        null
    }

    JvmSettingPageScaffold(
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3
        ),
        topBar = topBar,
    ) {
        JvmSettingPageHeader(
            title = interfaceSettingsTitle,
            description = "把主题、背景和桌面显示偏好放进同一张可预览的控制台，保持紧凑，同时能快速看到当前视觉结果。",
        ) {
            JvmSettingStatusCard(
                width = JvmSettingSummaryCardWidth,
                prominentValue = true,
                items = listOf(
                    JvmSettingStatusCardItem(label = "主题", value = themeLabel),
                    JvmSettingStatusCardItem(label = "背景图片", value = backgroundStatus),
                    JvmSettingStatusCardItem(label = "强调色", value = "Material 主题色"),
                )
            )
        }

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = "主题外观",
                    subtitle = "用卡片呈现最终视觉倾向，而不是只给一个下拉菜单。",
                    badge = "桌面预览",
                    contentContainerEnabled = false,
                ) {
                    JvmSettingActionEntryGrid(
                        actionEntries = listOf(
                            JvmSettingActionEntry(
                                icon = Res.drawable.settings_24px,
                                kicker = "AUTO",
                                title = stringResource(ThemeTypeEnum.SYSTEM.toResStringInt()),
                                description = "根据系统深浅色自动切换界面。",
                                status = if (themeType == ThemeTypeEnum.SYSTEM) "当前主题" else "点击切换",
                                selected = themeType == ThemeTypeEnum.SYSTEM,
                                role = Role.RadioButton,
                                onClick = {
                                    if (themeType != ThemeTypeEnum.SYSTEM) {
                                        interfaceSettingViewModel.setThemeTypeData(ThemeTypeEnum.SYSTEM)
                                    }
                                },
                            ),
                            JvmSettingActionEntry(
                                icon = Res.drawable.settings_24px,
                                kicker = "DARK",
                                title = stringResource(ThemeTypeEnum.DARK.toResStringInt()),
                                description = "适合长时间播放和夜间桌面环境。",
                                status = if (themeType == ThemeTypeEnum.DARK) "当前主题" else "点击切换",
                                selected = themeType == ThemeTypeEnum.DARK,
                                role = Role.RadioButton,
                                onClick = {
                                    if (themeType != ThemeTypeEnum.DARK) {
                                        interfaceSettingViewModel.setThemeTypeData(ThemeTypeEnum.DARK)
                                    }
                                },
                            ),
                            JvmSettingActionEntry(
                                icon = Res.drawable.settings_24px,
                                kicker = "LIGHT",
                                title = stringResource(ThemeTypeEnum.LIGHT.toResStringInt()),
                                description = "适合白天办公和浅色系统主题。",
                                status = if (themeType == ThemeTypeEnum.LIGHT) "当前主题" else "点击切换",
                                selected = themeType == ThemeTypeEnum.LIGHT,
                                role = Role.RadioButton,
                                onClick = {
                                    if (themeType != ThemeTypeEnum.LIGHT) {
                                        interfaceSettingViewModel.setThemeTypeData(ThemeTypeEnum.LIGHT)
                                    }
                                },
                            ),
                        ),
                        fillTwoColumnWidth = true,
                    )
                }

                JvmSettingSection(
                    title = "显示细节",
                    subtitle = "背景相关偏好保留为明确入口，避免和主题模式混在一个下拉列表里。",
                    badge = "偏好",
                    qualityNote = "背景图片会交给现有背景设置页面处理；此页只负责桌面界面偏好的总览与主题切换。"
                ) {
                    JvmSettingNavigationRow(
                        icon = Res.drawable.album_24px,
                        title = backgroundImageTitle,
                        description = "选择或清除主界面的背景图片。",
                        value = backgroundStatus,
                        onClick = {
                            navigator.navigate(SetBackgroundImage)
                        }
                    )
                }
            },
            rightContent = {
                JvmSettingSection(
                    title = "实时预览",
                    subtitle = "用缩略窗口模拟主题和背景设置生效后的桌面结构。",
                    badge = "Preview",
                    contentContainerEnabled = false,
                ) {
                    JvmInterfaceMiniPreview(
                        themeLabel = themeLabel,
                        backgroundConfigured = backgroundConfigured,
                    )
                }

                JvmSettingSection(
                    title = "强调色",
                    subtitle = "界面设置页跟随 MaterialTheme.colorScheme，不单独维护桌面色值。",
                    badge = "Theme",
                    contentContainerEnabled = false,
                ) {
                    JvmInterfaceAccentPreview()
                    JvmSettingNote(text = "所有间距、圆角和高亮状态优先使用 XyTheme.dimens 与 MaterialTheme.colorScheme，保持与其他 JVM 设置页一致。")
                }
            }
        )
    }
}

@Composable
private fun JvmInterfaceMiniPreview(
    themeLabel: String,
    backgroundConfigured: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = JvmSettingInterfaceMiniPreviewHeight),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = colorScheme.surfaceContainerLowest,
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.contentPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(XyTheme.dimens.corner))
                    .background(colorScheme.surfaceContainer)
                    .border(
                        BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.10f)),
                        RoundedCornerShape(XyTheme.dimens.corner)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(colorScheme.onSurface.copy(alpha = 0.05f))
                        .padding(horizontal = XyTheme.dimens.contentPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    JvmInterfacePill(text = "XyMusic")
                    Text(
                        text = themeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(142.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(86.dp)
                            .fillMaxSize()
                            .background(colorScheme.background)
                            .border(
                                BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.08f)),
                                RoundedCornerShape(0.dp)
                            )
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        colorScheme.primary.copy(alpha = if (backgroundConfigured) 0.24f else 0.12f),
                                        colorScheme.surfaceContainer.copy(alpha = 0.0f),
                                    )
                                )
                            )
                            .padding(XyTheme.dimens.contentPadding),
                        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
                    ) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (index == 0) 1f else 0.82f)
                                    .height(34.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                colorScheme.primary.copy(alpha = 0.26f - index * 0.04f),
                                                colorScheme.onSurface.copy(alpha = 0.07f),
                                            )
                                        ),
                                        shape = RoundedCornerShape(XyTheme.dimens.outerVerticalPadding)
                                    )
                                    .border(
                                        BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.08f)),
                                        RoundedCornerShape(XyTheme.dimens.outerVerticalPadding)
                                    )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .background(colorScheme.background)
                        .border(
                            BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.08f)),
                            RoundedCornerShape(0.dp)
                        )
                )
            }

            JvmInterfacePreviewStatus(
                label = "背景状态",
                value = if (backgroundConfigured) "已连接背景图" else "使用主题背景"
            )
        }
    }
}

@Composable
private fun JvmInterfaceAccentPreview() {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.onSurface.copy(alpha = 0.07f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            Text(
                text = "当前界面色板",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            JvmSettingFlowRow(
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
            ) {
                listOf(
                    "Primary" to colorScheme.primary,
                    "Secondary" to colorScheme.secondary,
                    "Tertiary" to colorScheme.tertiary,
                ).forEach { (label, color) ->
                    JvmInterfaceColorChip(label = label, color = color)
                }
            }
        }
    }
}

@Composable
private fun JvmInterfaceColorChip(
    label: String,
    color: Color,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun JvmInterfacePreviewStatus(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        JvmInterfacePill(text = value)
    }
}

@Composable
private fun JvmInterfacePill(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
        )
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.outerVerticalPadding / 2
            ),
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
