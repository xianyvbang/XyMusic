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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.enums.TranscodeAudioBitRateType
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.ui.components.MusicSettingSwitchItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemRadioButton
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.viewmodel.StreamingQualityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamingQualityScreen(
    streamingQualityViewModel: StreamingQualityViewModel = hiltViewModel<StreamingQualityViewModel>(),
) {

    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = streamingQualityViewModel.backgroundConfig.cacheLimitBrash[0],
            bottomVerticalColor = streamingQualityViewModel.backgroundConfig.cacheLimitBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = "在线音乐品质"
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navigator.goBack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_setting_screen)
                    )
                }
            })

        LazyColumnNotComponent(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
            contentPadding = PaddingValues()
        ) {

            item {
                SettingRoundedSurfaceColumn {
                    MusicSettingSwitchItemComponent(
                        title = stringResource(R.string.any_network),
                        ifChecked = streamingQualityViewModel.ifTranscoding
                    ) { bol ->
                        coroutineScope.launch {
                            streamingQualityViewModel.updateIfTranscoding(bol)
                        }
                    }
                }

            }

            item {
                XyRow(
                    paddingValues = PaddingValues(
                        start = XyTheme.dimens.outerHorizontalPadding,
                        end = XyTheme.dimens.outerHorizontalPadding,
                        top = XyTheme.dimens.outerVerticalPadding
                    ),
                    horizontalArrangement = Arrangement.Start
                ) {
                    XyText(
                        text = stringResource(R.string.mobile_network_playback_sound_quality)
                    )
                }

            }

            item {
                SettingRoundedSurfaceColumn {
                    TranscodeAudioBitRateType.entries.forEach {
                        XyItemRadioButton(
                            text = it.audioBitRateStr,
                            selected = streamingQualityViewModel.mobileNetworkAudioBitRate == it,
                            onClick = {
                                coroutineScope.launch {
                                    streamingQualityViewModel.updateMobileNetworkAudioBitRate(it)
                                }
                            })
                    }
                }
            }

            item {
                XyRow(
                    paddingValues = PaddingValues(
                        start = XyTheme.dimens.outerHorizontalPadding,
                        end = XyTheme.dimens.outerHorizontalPadding,
                        top = XyTheme.dimens.outerVerticalPadding
                    ),
                    horizontalArrangement = Arrangement.Start
                ) {
                    XyText(
                        text = stringResource(R.string.wifi_network)
                    )
                }

            }

            item {
                SettingRoundedSurfaceColumn {
                    TranscodeAudioBitRateType.entries.forEach {
                        XyItemRadioButton(
                            text = it.audioBitRateStr,
                            selected = streamingQualityViewModel.wifiNetworkAudioBitRate == it,
                            onClick = {
                                coroutineScope.launch {
                                    streamingQualityViewModel.updateWifiNetworkAudioBitRate(it)
                                }
                            })
                    }
                }
            }



            item {
                XyRow(
                    paddingValues = PaddingValues(
                        start = XyTheme.dimens.outerHorizontalPadding,
                        end = XyTheme.dimens.outerHorizontalPadding,
                        top = XyTheme.dimens.outerVerticalPadding
                    ),
                    horizontalArrangement = Arrangement.Start
                ) {
                    XyText(
                        text = stringResource(R.string.transcoding_format)
                    )
                }

            }

            item {
                SettingRoundedSurfaceColumn {
                    streamingQualityViewModel.transcodeAudioBitRateType.forEach {
                        XyItemRadioButton(
                            text = it.name,
                            selected = streamingQualityViewModel.transcodeFormat == it.targetFormat,
                            onClick = {
                                coroutineScope.launch {
                                    streamingQualityViewModel.updateTranscodeFormat(it.targetFormat)
                                }
                            })

                    }
                }
            }
        }
    }
}

