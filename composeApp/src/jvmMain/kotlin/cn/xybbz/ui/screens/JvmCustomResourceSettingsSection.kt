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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.ui.components.JvmSettingContentGridTwoColumnBreakpoint
import cn.xybbz.ui.components.JvmSettingGroupVerticalGap
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingSwitchRow
import cn.xybbz.ui.components.JvmSettingWideGroup
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.CustomLyricsViewModel
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.custom_cover_api_hint
import xymusic.composeapp.generated.resources.jvm_connection_new_screen_text_28
import xymusic.composeapp.generated.resources.jvm_custom_api_screen_text_13
import xymusic.composeapp.generated.resources.jvm_custom_api_screen_text_19
import xymusic.composeapp.generated.resources.jvm_custom_api_screen_text_22
import xymusic.composeapp.generated.resources.jvm_custom_api_screen_text_23
import xymusic.composeapp.generated.resources.jvm_custom_api_screen_text_24
import xymusic.composeapp.generated.resources.jvm_custom_api_screen_text_49
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_63
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_64
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_65
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_66
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_67
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_68
import xymusic.composeapp.generated.resources.lyrics_single_api_hint
import xymusic.composeapp.generated.resources.prioritize_music_service_api
import xymusic.composeapp.generated.resources.restore_default
import xymusic.composeapp.generated.resources.save
import xymusic.composeapp.generated.resources.settings_24px

/** 接口地址与令牌输入框的最小高度，与设置页其它输入控件保持一致的行高观感。 */
private val JvmCustomResourceInputMinHeight = 40.dp

/** 自定义资源操作按钮的最小高度。 */
private val JvmCustomResourceButtonHeight = 32.dp

/** 字段小标题字号，对齐设计稿的紧凑小标签排版。 */
private val JvmCustomResourceFieldLabelSize = 10.sp

/**
 * 设置页内嵌的自定义资源分区。
 *
 * 自上而下依次为请求策略与接口配置两个分组：
 * 请求策略承接原自定义 API 设置页的优先级开关，开关即时写入设置；
 * 接口配置包含歌词接口地址、封面接口地址与访问令牌三个横向排列的输入框，
 * 保存与恢复默认是接口配置共用的一个操作区。
 * 外观复用设置页的分组标题、输入框与按钮样式，访问令牌下方的说明会自行换行展示 Authorization 请求头信息。
 *
 * @param customLyricsViewModel 自定义资源 ViewModel，负责接口地址与令牌的读写和保存。
 * @param modifier 外层布局修饰符，通常由设置页分组网格下发宽度。
 */
@Composable
internal fun JvmCustomResourceSettingsSection(
    customLyricsViewModel: CustomLyricsViewModel,
    modifier: Modifier = Modifier,
) {
    // 桌面端用系统浏览器打开自定义接口官方文档，沿用原自定义 API 设置页的跳转行为。
    val uriHandler = LocalUriHandler.current
    // 鉴权说明末尾追加可点击的“官方文档”，点击后打开自定义接口文档地址。
    val authSupportingText = buildAnnotatedString {
        append(
            stringResource(
                Res.string.jvm_setting_screen_text_68,
                ApiConstants.AUTHORIZATION,
            )
        )
        withLink(
            LinkAnnotation.Clickable(
                tag = "custom_resource_doc",
                linkInteractionListener = {
                    uriHandler.openUri(customLyricsViewModel.url)
                },
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.None,
                    )
                )
            )
        ) {
            append(stringResource(Res.string.jvm_custom_api_screen_text_49))
        }
        append("。")
    }

    // 三个输入框除取值与说明外结构一致，先组装成数据再统一渲染。
    val fields = listOf(
        JvmCustomResourceFieldData(
            label = stringResource(Res.string.jvm_setting_screen_text_64),
            value = customLyricsViewModel.customLrcSingleApiValue,
            hint = "${ApiConstants.HTTPS}${stringResource(Res.string.lyrics_single_api_hint)}",
            supportingText = AnnotatedString(stringResource(Res.string.jvm_setting_screen_text_66)),
            onValueChange = customLyricsViewModel::updateCustomLrcSingleApi,
        ),
        JvmCustomResourceFieldData(
            label = stringResource(Res.string.jvm_setting_screen_text_65),
            value = customLyricsViewModel.customCoverApiValue,
            hint = "${ApiConstants.HTTPS}${stringResource(Res.string.custom_cover_api_hint)}",
            supportingText = AnnotatedString(stringResource(Res.string.jvm_setting_screen_text_67)),
            onValueChange = customLyricsViewModel::updateCustomCoverApi,
        ),
        JvmCustomResourceFieldData(
            label = stringResource(Res.string.jvm_setting_screen_text_63),
            value = customLyricsViewModel.customLrcApiAuthValue,
            hint = stringResource(Res.string.jvm_connection_new_screen_text_28),
            // 令牌由歌词与封面接口共用，说明文案会换行展示请求头信息与官方文档入口。
            supportingText = authSupportingText,
            onValueChange = customLyricsViewModel::updateCustomLrcApiAuth,
        ),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(JvmSettingGroupVerticalGap),
    ) {
        // 请求策略分组：迁移原自定义 API 设置页的优先级开关，开关即时写入设置。
        JvmSettingWideGroup(title = stringResource(Res.string.jvm_custom_api_screen_text_19)) {
            JvmSettingSwitchRow(
                icon = Res.drawable.settings_24px,
                title = stringResource(Res.string.prioritize_music_service_api),
                description = stringResource(Res.string.jvm_custom_api_screen_text_24),
                checked = customLyricsViewModel.ifPriorityMusicApi,
                onCheckedChange = { checked ->
                    customLyricsViewModel.updateIfPriorityMusicApi(checked)
                    customLyricsViewModel.saveIfPriorityMusicApiSetting()
                },
            )
            // 说明当前生效的资源读取顺序，随开关状态实时切换。
            JvmSettingNote(
                text = if (customLyricsViewModel.ifPriorityMusicApi) {
                    stringResource(Res.string.jvm_custom_api_screen_text_22)
                } else {
                    stringResource(Res.string.jvm_custom_api_screen_text_23)
                }
            )
        }

        // 接口配置分组：三个输入框加一组共用的保存与恢复默认操作。
        JvmSettingWideGroup(title = stringResource(Res.string.jvm_custom_api_screen_text_13)) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                // 宽窗口下三个输入框等宽并排，窄窗口改为纵向堆叠保证输入宽度。
                if (maxWidth >= JvmSettingContentGridTwoColumnBreakpoint) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(JvmSettingGroupVerticalGap),
                        verticalAlignment = Alignment.Top,
                    ) {
                        fields.forEach { field ->
                            Box(modifier = Modifier.weight(1f)) {
                                JvmCustomResourceField(field = field)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(JvmSettingGroupVerticalGap),
                    ) {
                        fields.forEach { field ->
                            JvmCustomResourceField(field = field)
                        }
                    }
                }
            }

            // 整个分区共用一个操作区：保存写入全部配置，恢复默认清空三个输入。
            Row(
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                JvmCustomResourceActionButton(
                    text = stringResource(Res.string.save),
                    primary = true,
                    onClick = customLyricsViewModel::saveSettings,
                )
                JvmCustomResourceActionButton(
                    text = stringResource(Res.string.restore_default),
                    primary = false,
                    onClick = {
                        // 恢复默认：清空两个接口地址与共用令牌，并立即写回本地设置。
                        customLyricsViewModel.updateCustomLrcSingleApi("")
                        customLyricsViewModel.updateCustomCoverApi("")
                        customLyricsViewModel.updateCustomLrcApiAuth("")
                        customLyricsViewModel.saveSettings()
                    },
                )
            }
        }
    }
}

/**
 * 单个自定义资源输入项。
 *
 * 小标题在上、输入框居中，说明文案在下方换行展示。
 *
 * @param field 输入项展示数据与回调。
 */
@Composable
private fun JvmCustomResourceField(field: JvmCustomResourceFieldData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
    ) {
        JvmCustomResourceFieldLabel(text = field.label)
        JvmCustomResourceInput(
            value = field.value,
            hint = field.hint,
            onValueChange = field.onValueChange,
        )
        // 说明文案可能是带“官方文档”链接的富文本，这里渲染富文本重载。
        XyTextSub(
            text = field.supportingText,
            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 17.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 自定义资源输入项的小标题。
 *
 * @param text 小标题文案。
 */
@Composable
private fun JvmCustomResourceFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = JvmCustomResourceFieldLabelSize,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * 自定义资源接口输入框。
 *
 * 复用设置页输入框的圆角底色，内容使用等宽字体便于核对地址与令牌。
 *
 * @param value 当前输入值。
 * @param hint 占位提示。
 * @param onValueChange 输入变更回调。
 */
@Composable
private fun JvmCustomResourceInput(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
) {
    XyEdit(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = JvmCustomResourceInputMinHeight),
        text = value,
        onChange = onValueChange,
        hint = hint,
        // 输入框自带圆角底色，外层不再叠加内边距。
        paddingValues = PaddingValues(),
        backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace,
        ),
        textContentAlignment = Alignment.CenterStart,
        singleLine = true,
    )
}

/**
 * 自定义资源的操作按钮。
 *
 * @param text 按钮文案。
 * @param primary 是否为主操作（实心主色）；false 为描边次操作。
 * @param onClick 点击回调。
 */
@Composable
private fun JvmCustomResourceActionButton(
    text: String,
    primary: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = RoundedCornerShape(6.dp)
    val containerColor = if (primary) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = if (hovered) 0.12f else 0.06f)
    }
    val contentColor = if (primary) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val border = if (primary) {
        null
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    }

    Surface(
        modifier = Modifier.jvmHoverDebounceClickable(
            interactionSource = interactionSource,
            onClick = onClick,
        ),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = JvmCustomResourceButtonHeight)
                .padding(
                    horizontal = XyTheme.dimens.contentPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding / 2,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 自定义资源输入项的展示数据与回调。
 *
 * @property label 输入项小标题。
 * @property value 输入项当前值。
 * @property hint 输入框占位提示。
 * @property supportingText 输入框下方的说明文案，可包含可点击的官方文档链接。
 * @property onValueChange 输入变更回调。
 */
private data class JvmCustomResourceFieldData(
    val label: String,
    val value: String,
    val hint: String,
    val supportingText: AnnotatedString,
    val onValueChange: (String) -> Unit,
)
