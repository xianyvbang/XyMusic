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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.xybbz.common.enums.TranscodeAudioBitRateType
import cn.xybbz.ui.components.JvmLazyListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemRadioButton
import cn.xybbz.viewmodel.StreamingQualityViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.online_music_quality
import xymusic_kmp.composeapp.generated.resources.transcoding_format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmStreamingQualityScreen(
    streamingQualityViewModel: StreamingQualityViewModel = koinViewModel<StreamingQualityViewModel>(),
) {

    val coroutineScope = rememberCoroutineScope()

    XyColumnScreen {
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.online_music_quality)
                )
            })

        JvmLazyListComponent(
            modifier = Modifier.fillMaxSize(),
            pagingItems = null,
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            lazyColumnBottom = null
        ) {
            item {
                JvmStreamingQualityPanel(
                    title = stringResource(Res.string.online_music_quality)
                ) {
                    TranscodeAudioBitRateType.entries.forEach {
                        XyItemRadioButton(
                            text = it.audioBitRateStr,
                            selected = streamingQualityViewModel.wifiNetworkAudioBitRate == it,
                            onClick = {
                                coroutineScope.launch {
                                    streamingQualityViewModel.updateWifiNetworkAudioBitRate(it)
                                    streamingQualityViewModel.updateMobileNetworkAudioBitRate(it)
                                }
                            })
                    }
                }
            }

            item {
                JvmStreamingQualityPanel(
                    title = stringResource(Res.string.transcoding_format)
                ) {
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

@Composable
private fun JvmStreamingQualityPanel(
    title: String? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .widthIn(max = 760.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}
