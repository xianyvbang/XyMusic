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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.ui.components.JvmSettingFlowRow
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingPageContentMaxWidth
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingSwitchRow
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.viewmodel.CustomLyricsViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album_24px
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.custom_cover_api
import xymusic_kmp.composeapp.generated.resources.custom_cover_api_hint
import xymusic_kmp.composeapp.generated.resources.customize_lyric_settings
import xymusic_kmp.composeapp.generated.resources.http_24px
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.lyrics_api_auth_key
import xymusic_kmp.composeapp.generated.resources.lyrics_api_auth_key_hint
import xymusic_kmp.composeapp.generated.resources.lyrics_single_api
import xymusic_kmp.composeapp.generated.resources.lyrics_single_api_hint
import xymusic_kmp.composeapp.generated.resources.music_note_24px
import xymusic_kmp.composeapp.generated.resources.password_24px
import xymusic_kmp.composeapp.generated.resources.prioritize_music_service_api
import xymusic_kmp.composeapp.generated.resources.save
import xymusic_kmp.composeapp.generated.resources.settings_24px

private val JvmCustomApiSummaryWidth = 268.dp
private val JvmCustomApiEndpointGridBreakpoint = 620.dp
private val JvmCustomApiTokenGridBreakpoint = 460.dp

@Composable
fun JvmCustomApiScreen(
    customLyricsViewModel: CustomLyricsViewModel = koinViewModel<CustomLyricsViewModel>()
) {
    val uriHandler = LocalUriHandler.current
    val pageTitle = stringResource(Res.string.customize_lyric_settings)
    val saveTitle = stringResource(Res.string.save)
    val lyricsApiTitle = stringResource(Res.string.lyrics_single_api)
    val coverApiTitle = stringResource(Res.string.custom_cover_api)
    val authTitle = stringResource(Res.string.lyrics_api_auth_key)

    val lyricsConfigured = customLyricsViewModel.customLrcSingleApiValue.isNotBlank()
    val coverConfigured = customLyricsViewModel.customCoverApiValue.isNotBlank()
    val authConfigured = customLyricsViewModel.customLrcApiAuthValue.isNotBlank()
    val configuredEndpointCount = listOf(lyricsConfigured, coverConfigured).count { it }
    val priorityLabel = if (customLyricsViewModel.ifPriorityMusicApi) {
        "音乐服务优先"
    } else {
        "自定义资源优先"
    }
    val authLabel = if (authConfigured) "Header 已配置" else "未设置 Header"
    val authHelpText = customApiAuthHelpText(
        docUrl = customLyricsViewModel.url,
        onOpenDoc = {
            uriHandler.openUri(customLyricsViewModel.url)
        }
    )

    JvmSettingPageScaffold(
        contentMaxWidth = JvmSettingPageContentMaxWidth,
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3
        )
    ) {
        JvmSettingPageHeader(
            title = pageTitle,
            description = "统一管理桌面端歌词与封面自定义接口。配置为空时继续使用音乐服务返回的资源，保存后会应用到后续播放和封面解析流程。",
            contentMaxWidth = JvmSettingPageContentMaxWidth,
        ) {
            JvmCustomApiSummaryCard(
                configuredEndpointCount = configuredEndpointCount,
                priorityLabel = priorityLabel,
                authLabel = authLabel,
                saveTitle = saveTitle,
                onSave = {
                    customLyricsViewModel.saveSettings()
                }
            )
        }

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = "服务端点",
                    subtitle = "按用途展示当前自定义资源端点，便于桌面端快速扫描配置状态。",
                    badge = "CustomMediaApi",
                    contentContainerEnabled = false,
                    qualityNote = "自定义接口请求失败或未配置时，会继续回退到当前音乐服务返回的歌词和封面，避免影响播放主流程。",
                ) {
                    JvmCustomApiEndpointGrid(
                        endpoints = listOf(
                            JvmCustomApiEndpoint(
                                icon = Res.drawable.music_note_24px,
                                title = lyricsApiTitle,
                                description = "为当前播放歌曲补充网络歌词文本。",
                                url = customLyricsViewModel.customLrcSingleApiValue,
                                badge = if (customLyricsViewModel.ifPriorityMusicApi) "回退" else "优先",
                                configured = lyricsConfigured,
                            ),
                            JvmCustomApiEndpoint(
                                icon = Res.drawable.album_24px,
                                title = coverApiTitle,
                                description = "为歌曲、专辑和艺术家补充封面地址。",
                                url = customLyricsViewModel.customCoverApiValue,
                                badge = if (customLyricsViewModel.ifPriorityMusicApi) "回退" else "优先",
                                configured = coverConfigured,
                            )
                        )
                    )
                }

                JvmSettingSection(
                    title = "接口配置",
                    subtitle = "请求地址和鉴权信息会在保存时写入本地设置，播放链路读取保存后的配置。",
                    badge = "编辑",
                    contentContainerEnabled = false,
                ) {
                    JvmCustomApiFieldList(
                        fields = listOf(
                            JvmCustomApiField(
                                icon = Res.drawable.music_note_24px,
                                title = lyricsApiTitle,
                                description = "请求成功后直接读取接口返回的歌词文本。",
                                value = customLyricsViewModel.customLrcSingleApiValue,
                                hint = stringResource(Res.string.lyrics_single_api_hint),
                                badge = "GET / lyrics",
                                onValueChange = customLyricsViewModel::updateCustomLrcSingleApi,
                            ),
                            JvmCustomApiField(
                                icon = Res.drawable.album_24px,
                                title = coverApiTitle,
                                description = "封面接口需要返回可访问图片地址，客户端会把它作为自定义封面候选。",
                                value = customLyricsViewModel.customCoverApiValue,
                                hint = stringResource(Res.string.custom_cover_api_hint),
                                badge = "GET / cover",
                                onValueChange = customLyricsViewModel::updateCustomCoverApi,
                            ),
                            JvmCustomApiField(
                                icon = Res.drawable.password_24px,
                                title = authTitle,
                                description = "同一份鉴权信息会用于歌词和封面请求。",
                                value = customLyricsViewModel.customLrcApiAuthValue,
                                hint = stringResource(Res.string.lyrics_api_auth_key_hint),
                                badge = ApiConstants.AUTHORIZATION,
                                supportingText = authHelpText,
                                onValueChange = customLyricsViewModel::updateCustomLrcApiAuth,
                            )
                        )
                    )
                }
            },
            rightContent = {
                JvmSettingSection(
                    title = "请求策略",
                    subtitle = "控制自定义服务和音乐服务在主流程中的先后顺序。",
                    badge = "优先级",
                    qualityNote = if (customLyricsViewModel.ifPriorityMusicApi) {
                        "当前先读取音乐服务接口；服务端没有歌词或封面时，再使用自定义接口兜底。"
                    } else {
                        "当前先读取自定义接口；自定义接口失败时，再回退到音乐服务返回的资源。"
                    },
                ) {
                    JvmSettingSwitchRow(
                        icon = Res.drawable.settings_24px,
                        title = stringResource(Res.string.prioritize_music_service_api),
                        description = "开启后音乐服务优先，关闭后自定义接口优先。",
                        checked = customLyricsViewModel.ifPriorityMusicApi,
                        onCheckedChange = customLyricsViewModel::updateIfPriorityMusicApi,
                    )
                }

                JvmSettingSection(
                    title = "请求参数",
                    subtitle = "客户端会把当前歌曲信息作为查询参数追加到配置的 URL。",
                    badge = "参数",
                    contentContainerEnabled = false,
                ) {
                    JvmCustomApiTokenGrid(
                        tokens = listOf(
                            JvmCustomApiToken(name = "title", description = "歌曲标题"),
                            JvmCustomApiToken(name = "artist", description = "艺术家名称"),
                            JvmCustomApiToken(name = "album", description = "专辑名称"),
                            JvmCustomApiToken(name = "path", description = "本地或服务端路径"),
                            JvmCustomApiToken(name = ApiConstants.AUTHORIZATION, description = "请求头鉴权"),
                            JvmCustomApiToken(
                                name = ApiConstants.CUSTOM_IMAGE_HEADER_NAME,
                                description = "鉴权参数兼容字段"
                            ),
                        )
                    )
                }

                JvmSettingSection(
                    title = "生效范围",
                    subtitle = "保存后的配置由歌词和封面解析链路统一读取。",
                    badge = "运行时",
                    contentContainerEnabled = false,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
                    ) {
                        JvmCustomApiRuntimeRow(
                            icon = Res.drawable.check_24px,
                            title = "播放歌词",
                            description = "当前播放歌曲没有内嵌歌词时，使用音乐服务和自定义接口补全。"
                        )
                        JvmCustomApiRuntimeRow(
                            icon = Res.drawable.http_24px,
                            title = "封面解析",
                            description = "歌曲、专辑和艺术家封面会按当前优先级选择候选地址。"
                        )
                        JvmSettingNote(text = "此页面只保存配置，不立即发起端点测试；新的配置会在后续播放和封面加载时生效。")
                    }
                }
            }
        )
    }
}

@Composable
private fun JvmCustomApiSummaryCard(
    configuredEndpointCount: Int,
    priorityLabel: String,
    authLabel: String,
    saveTitle: String,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier.width(JvmCustomApiSummaryWidth),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        JvmSettingStatusCard(
            width = JvmCustomApiSummaryWidth,
            items = listOf(
                JvmSettingStatusCardItem(label = "已配置端点", value = "$configuredEndpointCount / 2"),
                JvmSettingStatusCardItem(label = "当前优先", value = priorityLabel),
                JvmSettingStatusCardItem(label = "鉴权状态", value = authLabel),
            )
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            onClick = onSave,
            shape = RoundedCornerShape(XyTheme.dimens.corner),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        ) {
            Text(
                text = saveTitle,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun JvmCustomApiEndpointGrid(
    endpoints: List<JvmCustomApiEndpoint>,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding
        val cardWidth = if (maxWidth >= JvmCustomApiEndpointGridBreakpoint) {
            (maxWidth - gap) / 2f
        } else {
            maxWidth
        }

        JvmSettingFlowRow(
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            endpoints.forEach { endpoint ->
                JvmCustomApiEndpointCard(
                    modifier = Modifier.width(cardWidth),
                    endpoint = endpoint
                )
            }
        }
    }
}

@Composable
private fun JvmCustomApiEndpointCard(
    endpoint: JvmCustomApiEndpoint,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val accentColor = if (endpoint.configured) colorScheme.primary else colorScheme.onSurfaceVariant
    val statusLabel = if (endpoint.configured) "已配置" else "未配置"

    Surface(
        modifier = modifier.heightIn(min = 150.dp),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = if (endpoint.configured) {
            colorScheme.primary.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.16f else 0.08f)
        } else {
            colorScheme.onSurface.copy(alpha = 0.05f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (endpoint.configured) {
                colorScheme.primary.copy(alpha = 0.28f)
            } else {
                colorScheme.onSurface.copy(alpha = 0.08f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JvmCustomApiIcon(icon = endpoint.icon, color = accentColor)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = endpoint.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        modifier = Modifier.padding(top = XyTheme.dimens.outerVerticalPadding / 2),
                        text = endpoint.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                }
            }

            Text(
                text = endpoint.url.ifBlank { "未配置请求地址" },
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = if (endpoint.configured) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                JvmCustomApiPill(
                    text = endpoint.badge,
                    containerColor = colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = colorScheme.primary,
                )
                JvmCustomApiPill(
                    text = statusLabel,
                    containerColor = colorScheme.onSurface.copy(alpha = 0.08f),
                    contentColor = if (endpoint.configured) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun JvmCustomApiFieldList(
    fields: List<JvmCustomApiField>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        fields.forEach { field ->
            JvmCustomApiFieldCard(field = field)
        }
    }
}

@Composable
private fun JvmCustomApiFieldCard(
    field: JvmCustomApiField,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JvmCustomApiIcon(icon = field.icon, color = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f, fill = false),
                            text = field.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        JvmCustomApiPill(
                            text = field.badge,
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        modifier = Modifier.padding(top = XyTheme.dimens.outerVerticalPadding / 2),
                        text = field.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp
                    )
                }
            }

            JvmCustomApiInput(
                value = field.value,
                hint = field.hint,
                onValueChange = field.onValueChange,
            )

            field.supportingText?.let { supportingText ->
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    overflow = TextOverflow.Visible
                )
            }
        }
    }
}

@Composable
private fun JvmCustomApiInput(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
) {

    XyEdit(
        modifier = Modifier.fillMaxWidth(),
        text = value,
        onChange = onValueChange,
        hint = hint,
        paddingValues = PaddingValues(),
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Start
        ),
        textContentAlignment = Alignment.CenterStart,
        singleLine = true,
    )
}

@Composable
private fun JvmCustomApiTokenGrid(
    tokens: List<JvmCustomApiToken>,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding
        val tokenWidth = if (maxWidth >= JvmCustomApiTokenGridBreakpoint) {
            (maxWidth - gap) / 2f
        } else {
            maxWidth
        }

        JvmSettingFlowRow(
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            tokens.forEach { token ->
                JvmCustomApiTokenCard(
                    modifier = Modifier.width(tokenWidth),
                    token = token
                )
            }
        }
    }
}

@Composable
private fun JvmCustomApiTokenCard(
    token: JvmCustomApiToken,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.heightIn(min = 70.dp),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.contentPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
        ) {
            Text(
                text = token.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = token.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun JvmCustomApiRuntimeRow(
    icon: DrawableResource,
    title: String,
    description: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(XyTheme.dimens.contentPadding),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JvmCustomApiIcon(icon = icon, color = MaterialTheme.colorScheme.primary)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
private fun JvmCustomApiIcon(
    icon: DrawableResource,
    color: Color,
    size: Dp = 38.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(color = color.copy(alpha = 0.18f), shape = RoundedCornerShape(XyTheme.dimens.corner))
            .border(
                BorderStroke(width = 1.dp, color = color.copy(alpha = 0.26f)),
                RoundedCornerShape(XyTheme.dimens.corner)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(size * 0.56f),
            tint = color
        )
    }
}

@Composable
private fun JvmCustomApiPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.20f))
    ) {
        Text(
            modifier = Modifier
                .widthIn(max = 180.dp)
                .padding(
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

@Composable
private fun customApiAuthHelpText(
    docUrl: String,
    onOpenDoc: () -> Unit,
): AnnotatedString {
    return buildAnnotatedString {
        append("鉴权信息会作为 ${ApiConstants.AUTHORIZATION} 请求头传入，并兼容 ")
        append(ApiConstants.CUSTOM_IMAGE_HEADER_NAME)
        append(" 查询参数。更多信息请参考")
        withLink(
            LinkAnnotation.Clickable(
                tag = "custom_api_doc",
                linkInteractionListener = {
                    onOpenDoc()
                },
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.None
                    )
                )
            )
        ) {
            append("官方文档")
        }
        append("。")
    }
}

private data class JvmCustomApiEndpoint(
    val icon: DrawableResource,
    val title: String,
    val description: String,
    val url: String,
    val badge: String,
    val configured: Boolean,
)

private data class JvmCustomApiField(
    val icon: DrawableResource,
    val title: String,
    val description: String,
    val value: String,
    val hint: String,
    val badge: String,
    val supportingText: AnnotatedString? = null,
    val onValueChange: (String) -> Unit,
)

private data class JvmCustomApiToken(
    val name: String,
    val description: String,
)
