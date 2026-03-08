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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.ui.components.MusicSettingSwitchItemComponent
import cn.xybbz.ui.components.SettingParentItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyIconButton as IconButton
import cn.xybbz.viewmodel.CustomLyricsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomLyricsScreen(
    modifier: Modifier = Modifier,
    customLyricsViewModel: CustomLyricsViewModel = hiltViewModel()
) {
    val navigator = LocalNavigator.current
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
                    title = stringResource(R.string.customize_lyric_settings)
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navigator.goBack() },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_setting_screen)
                    )
                }
            },
            actions = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        customLyricsViewModel.saveSettings()
                    }
                }) {
                    Text(stringResource(R.string.save))
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
                        title = stringResource(R.string.prioritize_music_service_api),
                        ifChecked = customLyricsViewModel.ifPriorityMusicApi
                    ) { bol ->
                        customLyricsViewModel.updateIfPriorityMusicApi(bol)
                    }
                }
            }

            item {
                CustomLyricsSettingInput(
                    title = stringResource(R.string.lyrics_single_api),
                    bottomInfo = stringResource(R.string.lyrics_single_api_desc),
                    value = customLyricsViewModel.customLrcSingleApiValue,
                    hint = stringResource(R.string.lyrics_single_api_hint),
                    onValueChange = { customLyricsViewModel.updateCustomLrcSingleApi(it) }
                )
            }

            item {
                CustomLyricsSettingInput(
                    title = stringResource(R.string.lyrics_api_auth_key),
                    bottomInfo = stringResource(R.string.lyrics_api_auth_key_desc),
                    value = customLyricsViewModel.customLrcApiAuthValue,
                    hint = stringResource(R.string.lyrics_api_auth_key_hint),
                    onValueChange = { customLyricsViewModel.updateCustomLrcApiAuth(it) }
                )
            }

            item {
                CustomLyricsSettingInput(
                    title = stringResource(R.string.custom_cover_api),
                    bottomInfo = stringResource(R.string.custom_cover_api_desc),
                    value = customLyricsViewModel.customCoverApiValue,
                    hint = stringResource(R.string.custom_cover_api_hint),
                    onValueChange = { customLyricsViewModel.updateCustomCoverApi(it) }
                )
            }
        }
    }
}

@Composable
private fun CustomLyricsSettingInput(
    title: String,
    bottomInfo: String,
    value: TextFieldValue,
    hint: String,
    onValueChange: (String) -> Unit
) {
    // 通用设置输入行：左侧标题说明，右侧单行输入框
    SettingRoundedSurfaceColumn {
        SettingParentItemComponent(
            title = title,
            bottomInfo = bottomInfo,
            trailingContent = {
                Row(
                    modifier = Modifier.width(220.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    XyEdit(
                        text = value,
                        onChange = { newValue -> onValueChange(newValue.text) },
                        hint = hint,
                        paddingValues = PaddingValues(),
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End
                        ),
                        textContentAlignment = Alignment.CenterEnd,
                        singleLine = true,
                    )
                }
            }
        )
    }

}
