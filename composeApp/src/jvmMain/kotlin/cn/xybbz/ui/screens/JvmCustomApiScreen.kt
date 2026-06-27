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
import cn.xybbz.ui.components.JvmSettingContentGridTwoColumnBreakpoint
import cn.xybbz.ui.components.JvmSettingFlowRow
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingSwitchRow
import cn.xybbz.ui.components.JvmSettingSummaryCardWidth
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.viewmodel.CustomLyricsViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.*
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.album_24px
import xymusic.composeapp.generated.resources.check_24px
import xymusic.composeapp.generated.resources.custom_cover_api
import xymusic.composeapp.generated.resources.custom_cover_api_hint
import xymusic.composeapp.generated.resources.customize_lyric_settings
import xymusic.composeapp.generated.resources.http_24px
import xymusic.composeapp.generated.resources.info_24px
import xymusic.composeapp.generated.resources.lyrics_api_auth_key
import xymusic.composeapp.generated.resources.lyrics_api_auth_key_hint
import xymusic.composeapp.generated.resources.lyrics_single_api
import xymusic.composeapp.generated.resources.lyrics_single_api_hint
import xymusic.composeapp.generated.resources.music_note_24px
import xymusic.composeapp.generated.resources.password_24px
import xymusic.composeapp.generated.resources.prioritize_music_service_api
import xymusic.composeapp.generated.resources.save
import xymusic.composeapp.generated.resources.settings_24px

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
        stringResource(Res.string.jvm_custom_api_screen_text_01)
    } else {
        stringResource(Res.string.jvm_custom_api_screen_text_02)
    }
    val authLabel = if (authConfigured) stringResource(Res.string.jvm_custom_api_screen_text_03) else stringResource(Res.string.jvm_custom_api_screen_text_04)
    val authHelpText = customApiAuthHelpText(
        docUrl = customLyricsViewModel.url,
        onOpenDoc = {
            uriHandler.openUri(customLyricsViewModel.url)
        }
    )

    JvmSettingPageScaffold(
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3
        )
    ) {
        JvmSettingPageHeader(
            title = pageTitle,
            description = stringResource(Res.string.jvm_custom_api_screen_text_05),
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
                    title = stringResource(Res.string.jvm_custom_api_screen_text_06),
                    subtitle = stringResource(Res.string.jvm_custom_api_screen_text_07),
                    badge = "CustomMediaApi",
                    contentContainerEnabled = false,
                    qualityNote = stringResource(Res.string.jvm_custom_api_screen_text_08),
                ) {
                    JvmCustomApiEndpointGrid(
                        endpoints = listOf(
                            JvmCustomApiEndpoint(
                                icon = Res.drawable.music_note_24px,
                                title = lyricsApiTitle,
                                description = stringResource(Res.string.jvm_custom_api_screen_text_09),
                                url = customLyricsViewModel.customLrcSingleApiValue,
                                badge = if (customLyricsViewModel.ifPriorityMusicApi) stringResource(Res.string.jvm_custom_api_screen_text_10) else stringResource(Res.string.jvm_custom_api_screen_text_11),
                                configured = lyricsConfigured,
                            ),
                            JvmCustomApiEndpoint(
                                icon = Res.drawable.album_24px,
                                title = coverApiTitle,
                                description = stringResource(Res.string.jvm_custom_api_screen_text_12),
                                url = customLyricsViewModel.customCoverApiValue,
                                badge = if (customLyricsViewModel.ifPriorityMusicApi) stringResource(Res.string.jvm_custom_api_screen_text_10) else stringResource(Res.string.jvm_custom_api_screen_text_11),
                                configured = coverConfigured,
                            )
                        )
                    )
                }

                JvmSettingSection(
                    title = stringResource(Res.string.jvm_custom_api_screen_text_13),
                    subtitle = stringResource(Res.string.jvm_custom_api_screen_text_14),
                    badge = stringResource(Res.string.jvm_custom_api_screen_text_15),
                    contentContainerEnabled = false,
                ) {
                    JvmCustomApiFieldList(
                        fields = listOf(
                            JvmCustomApiField(
                                icon = Res.drawable.music_note_24px,
                                title = lyricsApiTitle,
                                description = stringResource(Res.string.jvm_custom_api_screen_text_16),
                                value = customLyricsViewModel.customLrcSingleApiValue,
                                hint = "${ApiConstants.HTTPS}${stringResource(Res.string.lyrics_single_api_hint)}",
                                badge = "GET / lyrics",
                                onValueChange = customLyricsViewModel::updateCustomLrcSingleApi,
                            ),
                            JvmCustomApiField(
                                icon = Res.drawable.album_24px,
                                title = coverApiTitle,
                                description = stringResource(Res.string.jvm_custom_api_screen_text_17),
                                value = customLyricsViewModel.customCoverApiValue,
                                hint = "${ApiConstants.HTTPS}${stringResource(Res.string.custom_cover_api_hint)}",
                                badge = "GET / cover",
                                onValueChange = customLyricsViewModel::updateCustomCoverApi,
                            ),
                            JvmCustomApiField(
                                icon = Res.drawable.password_24px,
                                title = authTitle,
                                description = stringResource(Res.string.jvm_custom_api_screen_text_18),
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
                    title = stringResource(Res.string.jvm_custom_api_screen_text_19),
                    subtitle = stringResource(Res.string.jvm_custom_api_screen_text_20),
                    badge = stringResource(Res.string.jvm_custom_api_screen_text_21),
                    qualityNote = if (customLyricsViewModel.ifPriorityMusicApi) {
                        stringResource(Res.string.jvm_custom_api_screen_text_22)
                    } else {
                        stringResource(Res.string.jvm_custom_api_screen_text_23)
                    },
                ) {
                    JvmSettingSwitchRow(
                        icon = Res.drawable.settings_24px,
                        title = stringResource(Res.string.prioritize_music_service_api),
                        description = stringResource(Res.string.jvm_custom_api_screen_text_24),
                        checked = customLyricsViewModel.ifPriorityMusicApi,
                        onCheckedChange = customLyricsViewModel::updateIfPriorityMusicApi,
                    )
                }

                JvmSettingSection(
                    title = stringResource(Res.string.jvm_custom_api_screen_text_25),
                    subtitle = stringResource(Res.string.jvm_custom_api_screen_text_26),
                    badge = stringResource(Res.string.jvm_custom_api_screen_text_27),
                    contentContainerEnabled = false,
                ) {
                    JvmCustomApiTokenGrid(
                        tokens = listOf(
                            JvmCustomApiToken(name = "title", description = stringResource(Res.string.jvm_custom_api_screen_text_28)),
                            JvmCustomApiToken(name = "artist", description = stringResource(Res.string.jvm_custom_api_screen_text_29)),
                            JvmCustomApiToken(name = "album", description = stringResource(Res.string.jvm_custom_api_screen_text_30)),
                            JvmCustomApiToken(name = "path", description = stringResource(Res.string.jvm_custom_api_screen_text_31)),
                            JvmCustomApiToken(name = ApiConstants.AUTHORIZATION, description = stringResource(Res.string.jvm_custom_api_screen_text_32)),
                            JvmCustomApiToken(
                                name = ApiConstants.CUSTOM_IMAGE_HEADER_NAME,
                                description = stringResource(Res.string.jvm_custom_api_screen_text_33)
                            ),
                        )
                    )
                }

                JvmSettingSection(
                    title = stringResource(Res.string.jvm_custom_api_screen_text_34),
                    subtitle = stringResource(Res.string.jvm_custom_api_screen_text_35),
                    badge = stringResource(Res.string.jvm_about_screen_text_10),
                    contentContainerEnabled = false,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
                    ) {
                        JvmCustomApiRuntimeRow(
                            icon = Res.drawable.check_24px,
                            title = stringResource(Res.string.jvm_custom_api_screen_text_36),
                            description = stringResource(Res.string.jvm_custom_api_screen_text_37)
                        )
                        JvmCustomApiRuntimeRow(
                            icon = Res.drawable.http_24px,
                            title = stringResource(Res.string.jvm_custom_api_screen_text_38),
                            description = stringResource(Res.string.jvm_custom_api_screen_text_39)
                        )
                        JvmSettingNote(text = stringResource(Res.string.jvm_custom_api_screen_text_40))
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
        modifier = Modifier.width(JvmSettingSummaryCardWidth),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        JvmSettingStatusCard(
            width = JvmSettingSummaryCardWidth,
            items = listOf(
                JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_custom_api_screen_text_41), value = "$configuredEndpointCount / 2"),
                JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_custom_api_screen_text_42), value = priorityLabel),
                JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_custom_api_screen_text_43), value = authLabel),
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
        val cardWidth = if (maxWidth >= JvmSettingContentGridTwoColumnBreakpoint) {
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
    val statusLabel = if (endpoint.configured) stringResource(Res.string.jvm_custom_api_screen_text_44) else stringResource(Res.string.jvm_custom_api_screen_text_45)

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
                text = endpoint.url.ifBlank { stringResource(Res.string.jvm_custom_api_screen_text_46) },
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
        val tokenWidth = if (maxWidth >= JvmSettingContentGridTwoColumnBreakpoint) {
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
        append(stringResource(Res.string.jvm_custom_api_screen_text_47, ApiConstants.AUTHORIZATION))
        append(ApiConstants.CUSTOM_IMAGE_HEADER_NAME)
        append(stringResource(Res.string.jvm_custom_api_screen_text_48))
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
            append(stringResource(Res.string.jvm_custom_api_screen_text_49))
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
