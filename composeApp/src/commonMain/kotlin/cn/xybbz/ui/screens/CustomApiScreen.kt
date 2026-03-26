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

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.ui.components.MusicSettingSwitchItemComponent
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.CustomLyricsViewModel
import kotlinx.coroutines.launch
import cn.xybbz.ui.xy.XyIconButton as IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomApiScreen(
    modifier: Modifier = Modifier,
    customLyricsViewModel: CustomLyricsViewModel = hiltViewModel()
) {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 页面整体：复用设置页背景渐变
    XyColumnScreen(
        modifier = modifier.brashColor(
            topVerticalColor = customLyricsViewModel.backgroundConfig.settingsBrash[0],
            bottomVerticalColor = customLyricsViewModel.backgroundConfig.settingsBrash[1]
        )
    ) {
        // 顶部栏：返回 + 保存
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.customize_lyric_settings)
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navigator.goBack() },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.return_setting_screen)
                    )
                }
            },
            actions = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        customLyricsViewModel.saveSettings()
                    }
                }) {
                    Text(stringResource(Res.string.save))
                }
            }
        )

        // 内容区：开关 + 接口配置项
        LazyColumnNotComponent(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
            contentPadding = PaddingValues()
        ) {
            item {
                SettingRoundedSurfaceColumn {
                    MusicSettingSwitchItemComponent(
                        title = stringResource(Res.string.prioritize_music_service_api),
                        ifChecked = customLyricsViewModel.ifPriorityMusicApi
                    ) { bol ->
                        customLyricsViewModel.updateIfPriorityMusicApi(bol)
                    }
                }
            }

            item {
                CustomLyricsSettingTitleItem(
                    title = stringResource(Res.string.lyrics_api_auth_key)
                )
            }
            item {
                val annotatedText = buildAnnotatedString {

                    append("验证信息作为请求头传入,使用${ApiConstants.AUTHORIZATION}作为Key为验证信息,更多信息请参考")

                    pushStringAnnotation(
                        tag = "CLICK",
                        annotation = "官方文档"
                    )
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "CLICK",
                            linkInteractionListener = {
                                val intent =
                                    Intent(Intent.ACTION_VIEW, customLyricsViewModel.url.toUri())
                                context.startActivity(intent)
                            },
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.None   // 去掉下划线
                                )
                            )
                        )
                    ) {
                        append("官方文档")
                    }
                }
                CustomLyricsItemComponent(
                    bottomInfo = annotatedText,
                    title = "验证信息",
                    value = customLyricsViewModel.customLrcApiAuthValue,
                    hint = stringResource(Res.string.lyrics_api_auth_key_hint),
                    onValueChange = { customLyricsViewModel.updateCustomLrcApiAuth(it) }
                )
            }

            item {
                CustomLyricsSettingTitleItem(
                    title = stringResource(Res.string.lyrics_single_api)
                )
            }
            item {
                CustomLyricsSettingInput(
                    value = customLyricsViewModel.customLrcSingleApiValue,
                    hint = stringResource(Res.string.lyrics_single_api_hint),
                    onValueChange = { customLyricsViewModel.updateCustomLrcSingleApi(it) }
                )
            }

            item {
                CustomLyricsSettingTitleItem(
                    title = stringResource(Res.string.custom_cover_api)
                )
            }
            item {
                CustomLyricsSettingInput(
                    value = customLyricsViewModel.customCoverApiValue,
                    hint = stringResource(Res.string.custom_cover_api_hint),
                    onValueChange = { customLyricsViewModel.updateCustomCoverApi(it) }
                )
            }
        }
    }
}

@Composable
private fun CustomLyricsSettingTitleItem(
    title: String,
) {
    XyRow(
        paddingValues = PaddingValues(
            start = XyTheme.dimens.outerHorizontalPadding,
            end = XyTheme.dimens.outerHorizontalPadding,
            top = XyTheme.dimens.outerVerticalPadding
        ),
        horizontalArrangement = Arrangement.Start
    ) {
        XyTextSub(
            text = title
        )
    }
}

@Composable
private fun CustomLyricsSettingInput(
    title: String = "地址",
    bottomInfo: String? = null,
    value: String,
    hint: String,
    onValueChange: (String) -> Unit
) {
    // 通用设置输入行：复用 SettingItem 样式，右侧单行输入框
    SettingRoundedSurfaceColumn {
        SettingItemComponent(
            title = title,
            bottomInfo = bottomInfo,
            painter = null,
            onRouter = {},
            trailingContent = {
                XyEdit(
                    modifier = Modifier.width(220.dp),
                    text = value,
                    onChange = { newValue -> onValueChange(newValue) },
                    hint = hint,
                    paddingValues = PaddingValues(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    ),
                    textContentAlignment = Alignment.CenterEnd,
                    singleLine = true,
                )
            }
        )
    }

}


@Composable
private fun CustomLyricsItemComponent(
    modifier: Modifier = Modifier,
    title: String = "地址",
    bottomInfo: AnnotatedString? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    value: String,
    hint: String,
    onValueChange: (String) -> Unit
) {
    SettingRoundedSurfaceColumn {
        XyColumn(
            modifier = modifier
                .heightIn(min = XyTheme.dimens.itemHeight)
                .fillMaxWidth(),
            paddingValues = PaddingValues(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
            ),
            backgroundColor = Color.Transparent,
            horizontalAlignment = Alignment.Start,
        ) {
            XyRow(paddingValues = PaddingValues(top = XyTheme.dimens.innerVerticalPadding), modifier = Modifier) {
                XyText(
                    text = title,
                    color = if (enabled) textColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                XyEdit(
                    modifier = Modifier.width(220.dp),
                    text = value,
                    onChange = { newValue -> onValueChange(newValue) },
                    hint = hint,
                    paddingValues = PaddingValues(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    ),
                    textContentAlignment = Alignment.CenterEnd,
                    singleLine = true,
                )
            }
            bottomInfo?.let {
                Spacer(modifier = Modifier.height(5.dp))
                XyTextSubSmall(
                    text = bottomInfo,
                    color = if (enabled) textColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Visible
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }

    }
}

