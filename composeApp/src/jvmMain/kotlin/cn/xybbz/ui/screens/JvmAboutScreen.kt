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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.ui.components.JvmSettingFlowRow
import cn.xybbz.ui.components.JvmSettingOverviewTile
import cn.xybbz.ui.components.JvmSettingPageContentMaxWidth
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.AboutViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.about
import xymusic_kmp.composeapp.generated.resources.add_link_24px
import xymusic_kmp.composeapp.generated.resources.app_icon_info
import xymusic_kmp.composeapp.generated.resources.app_name
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.current_version
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.function_not_implemented
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.logo_new
import xymusic_kmp.composeapp.generated.resources.music_note_24px
import xymusic_kmp.composeapp.generated.resources.no_official_website_yet
import xymusic_kmp.composeapp.generated.resources.official_website
import xymusic_kmp.composeapp.generated.resources.problem_feedback
import xymusic_kmp.composeapp.generated.resources.settings_24px

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun JvmAboutScreen(
    aboutViewModel: AboutViewModel = koinViewModel<AboutViewModel>()
) {
    LaunchedEffect(aboutViewModel) {
        aboutViewModel.getPlatformInfo()
    }

    val appName = stringResource(Res.string.app_name)
    val aboutTitle = stringResource(Res.string.about)
    val currentVersionTitle = stringResource(Res.string.current_version)
    val problemFeedbackTitle = stringResource(Res.string.problem_feedback)
    val officialWebsiteTitle = stringResource(Res.string.official_website)
    val functionNotImplemented = stringResource(Res.string.function_not_implemented)
    val noOfficialWebsiteYet = stringResource(Res.string.no_official_website_yet)
    val appIconInfo = stringResource(Res.string.app_icon_info)
    val versionInfo = aboutViewModel.versionInfo.ifBlank { "JVM" }

    JvmSettingPageScaffold(
        modifier = Modifier.fillMaxWidth(),
        contentMaxWidth = JvmSettingPageContentMaxWidth,
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3
        ),
        topBar = {
            TopAppBarComponent(
                title = {
                    TopAppBarTitle(title = aboutTitle)
                }
            )
        }
    ) {
        JvmAboutContent(
            appName = appName,
            appIconInfo = appIconInfo,
            versionInfo = versionInfo,
            currentVersionTitle = currentVersionTitle,
            problemFeedbackTitle = problemFeedbackTitle,
            officialWebsiteTitle = officialWebsiteTitle,
            onCheckUpdates = {
                MessageUtils.sendPopTip(functionNotImplemented)
            },
            onProblemFeedback = {
                MessageUtils.sendPopTip(functionNotImplemented)
            },
            onOfficialWebsite = {
                MessageUtils.sendPopTip(noOfficialWebsiteYet)
            },
            onCopyDiagnostics = {
                MessageUtils.sendPopTip(functionNotImplemented)
            }
        )
    }
}

@Composable
private fun JvmAboutContent(
    appName: String,
    appIconInfo: String,
    versionInfo: String,
    currentVersionTitle: String,
    problemFeedbackTitle: String,
    officialWebsiteTitle: String,
    onCheckUpdates: () -> Unit,
    onProblemFeedback: () -> Unit,
    onOfficialWebsite: () -> Unit,
    onCopyDiagnostics: () -> Unit,
) {
    Column(
        modifier = Modifier
            .widthIn(max = JvmSettingPageContentMaxWidth)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding * 2)
    ) {
        JvmAboutHero(
            appName = appName,
            appIconInfo = appIconInfo,
            versionInfo = versionInfo
        )

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = "应用信息",
                    subtitle = "版本、运行环境和支持的数据源集中展示。",
                    badge = "JVM Desktop",
                    contentContainerEnabled = false,
                    qualityNote = "桌面端版本信息来自 PlatformInfo；当前页面只调整关于页的信息架构和视觉密度。",
                ) {
                    JvmSettingFlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                    ) {
                        JvmSettingOverviewTile(
                            modifier = Modifier
                                .widthIn(min = 180.dp)
                                .weight(1f),
                            icon = Res.drawable.info_24px,
                            kicker = "版本",
                            value = versionInfo,
                            sub = currentVersionTitle
                        )
                        JvmSettingOverviewTile(
                            modifier = Modifier
                                .widthIn(min = 180.dp)
                                .weight(1f),
                            icon = Res.drawable.settings_24px,
                            kicker = "运行时",
                            value = "JVM",
                            sub = "Compose Multiplatform"
                        )
                        JvmSettingOverviewTile(
                            modifier = Modifier
                                .widthIn(min = 180.dp)
                                .weight(1f),
                            icon = Res.drawable.music_note_24px,
                            kicker = "数据源",
                            value = "5 类",
                            sub = "Navidrome / Jellyfin 等"
                        )
                    }
                }
            },
            rightContent = {
                JvmSettingSection(
                    title = "更新",
                    subtitle = "检查更新放在明显位置，但不打断设置浏览。",
                    badge = "Release",
                    contentContainerEnabled = false,
                ) {
                    JvmSettingFlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                    ) {
                        JvmAboutActionCard(
                            modifier = Modifier
                                .widthIn(min = 160.dp)
                                .weight(1f),
                            icon = Res.drawable.check_24px,
                            kicker = "Release",
                            title = "检查更新",
                            description = "获取最新桌面版本和更新说明。",
                            onClick = onCheckUpdates
                        )
                        JvmAboutActionCard(
                            modifier = Modifier
                                .widthIn(min = 160.dp)
                                .weight(1f),
                            icon = Res.drawable.download_24px,
                            kicker = "History",
                            title = "更新日志",
                            description = "查看最近版本的改动记录。",
                            onClick = onCheckUpdates
                        )
                    }
                }
            }
        )

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = "项目入口",
                    subtitle = "常用外部入口采用设置行样式，和主设置页的可点击项保持一致。",
                    badge = "Links",
                    contentContainerEnabled = false,
                ) {
                    JvmAboutSettingList {
                        JvmAboutSettingRow(
                            icon = Res.drawable.add_link_24px,
                            title = officialWebsiteTitle,
                            description = "查看项目主页、发布说明和文档入口。",
                            value = "打开",
                            onClick = onOfficialWebsite
                        )
                        JvmAboutSettingRow(
                            icon = Res.drawable.info_24px,
                            title = problemFeedbackTitle,
                            description = "提交桌面端问题、连接问题或功能建议。",
                            value = "反馈",
                            onClick = onProblemFeedback
                        )
                        JvmAboutSettingRow(
                            icon = Res.drawable.check_24px,
                            title = "复制诊断信息",
                            description = "包含版本、平台、数据源类型和 JVM 运行时。",
                            value = "复制",
                            onClick = onCopyDiagnostics
                        )
                    }
                }
            },
            rightContent = {
                JvmSettingSection(
                    title = "技术栈",
                    subtitle = "简洁列出核心依赖，方便排查桌面端问题。",
                    badge = "Stack",
                    contentContainerEnabled = false,
                ) {
                    JvmAboutSettingList {
                        JvmAboutInfoRow(
                            icon = Res.drawable.settings_24px,
                            title = "Kotlin Multiplatform",
                            description = "共享业务逻辑与平台实现。",
                            value = "KMP"
                        )
                        JvmAboutInfoRow(
                            icon = Res.drawable.info_24px,
                            title = "Compose Multiplatform",
                            description = "桌面端界面框架。",
                            value = "UI"
                        )
                        JvmAboutInfoRow(
                            icon = Res.drawable.music_note_24px,
                            title = "VLCJ / Media",
                            description = "桌面端播放能力。",
                            value = "Audio"
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun JvmAboutHero(
    appName: String,
    appIconInfo: String,
    versionInfo: String,
) {
    JvmSettingTwoPaneContent(
        leftContent = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(XyTheme.dimens.corner),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(XyTheme.dimens.innerHorizontalPadding + XyTheme.dimens.outerVerticalPadding),
                    horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.innerHorizontalPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    JvmAboutLogo(appIconInfo = appIconInfo)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
                    ) {
                        Text(
                            text = "桌面端设置",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.W900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "跨平台音乐客户端，面向自托管音乐服务和本地桌面播放体验。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        },
        rightContent = {
            JvmAboutStatusCard(
                versionInfo = versionInfo
            )
        }
    )
}

@Composable
private fun JvmAboutLogo(appIconInfo: String) {
    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(RoundedCornerShape(XyTheme.dimens.dialogCorner))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(68.dp),
            painter = painterResource(Res.drawable.logo_new),
            contentScale = ContentScale.Fit,
            contentDescription = appIconInfo
        )
    }
}

@Composable
private fun JvmAboutStatusCard(versionInfo: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 172.dp),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.innerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            JvmAboutStatusRow(label = "当前版本", value = versionInfo)
            JvmAboutStatusRow(label = "平台", value = "Windows Desktop")
            JvmAboutStatusRow(label = "更新状态", value = "未检查")
        }
    }
}

@Composable
private fun JvmAboutStatusRow(label: String, value: String) {
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
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun JvmAboutActionCard(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    kicker: String,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .heightIn(min = 118.dp)
            .jvmHoverDebounceClickable(onClick = onClick),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.contentPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JvmAboutSmallIcon(icon = icon, selected = true)
                Text(
                    text = kicker,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.W900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun JvmAboutSettingList(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
        )
    ) {
        Column(content = content)
    }
}

@Composable
private fun JvmAboutSettingRow(
    icon: DrawableResource,
    title: String,
    description: String,
    value: String,
    onClick: () -> Unit,
) {
    JvmAboutBaseRow(
        modifier = Modifier.jvmHoverDebounceClickable(onClick = onClick),
        icon = icon,
        title = title,
        description = description,
        trailing = {
            JvmAboutValuePill(value = value)
        }
    )
}

@Composable
private fun JvmAboutInfoRow(
    icon: DrawableResource,
    title: String,
    description: String,
    value: String,
) {
    JvmAboutBaseRow(
        icon = icon,
        title = title,
        description = description,
        trailing = {
            JvmAboutValuePill(value = value)
        }
    )
}

@Composable
private fun JvmAboutBaseRow(
    icon: DrawableResource,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(
                horizontal = XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JvmAboutSmallIcon(icon = icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        trailing()
    }
}

@Composable
private fun JvmAboutSmallIcon(
    icon: DrawableResource,
    selected: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                },
                shape = RoundedCornerShape(XyTheme.dimens.outerVerticalPadding)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun JvmAboutValuePill(value: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        )
    ) {
        Text(
            modifier = Modifier
                .widthIn(max = 128.dp)
                .padding(
                    horizontal = XyTheme.dimens.contentPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding
                ),
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
