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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cn.xybbz.common.enums.TranscodeAudioBitRateType
import cn.xybbz.ui.components.JvmSettingActionEntry
import cn.xybbz.ui.components.JvmSettingActionGridArrangement
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.StreamingQualityViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.online_music_quality
import xymusic_kmp.composeapp.generated.resources.settings_voice_24px
import xymusic_kmp.composeapp.generated.resources.transcoding_format
import xymusic_kmp.composeapp.generated.resources.volume_up_24px
import cn.xybbz.ui.components.JvmSettingActionGrid as JvmSettingActionEntryGrid

@Composable
fun JvmStreamingQualityScreen(
    streamingQualityViewModel: StreamingQualityViewModel = koinViewModel<StreamingQualityViewModel>(),
) {

    val coroutineScope = rememberCoroutineScope()
    val pageTitle = stringResource(Res.string.online_music_quality)
    val transcodeFormatTitle = stringResource(Res.string.transcoding_format)
    val selectedQuality = streamingQualityViewModel.wifiNetworkAudioBitRate
    val selectedFormatLabel = streamingQualityViewModel.transcodeAudioBitRateType
        .firstOrNull { it.targetFormat == streamingQualityViewModel.transcodeFormat }
        ?.name
        ?: streamingQualityViewModel.transcodeFormat.uppercase()

    JvmSettingPageScaffold() {
        JvmSettingPageHeader(
            title = pageTitle,
            description = "选择桌面端播放时使用的在线音频品质和服务端转码格式。当前桌面端使用一组播放品质设置，并同步应用到 Wi-Fi 与移动网络。",
        ) {
            JvmSettingStatusCard(
                items = listOf(
                    JvmSettingStatusCardItem(label = "播放品质", value = selectedQuality.audioBitRateStr),
                    JvmSettingStatusCardItem(label = "转码格式", value = selectedFormatLabel),
                    JvmSettingStatusCardItem(label = "应用范围", value = "全网络"),
                )
            )
        }

        JvmSettingSection(
            title = "播放品质",
            subtitle = "桌面端保持一组品质选择，写入时同步更新 Wi-Fi 与移动网络码率。",
            badge = "当前：全网络同步",
            titleMinWidth = 240.dp,
            contentContainerEnabled = false,
            qualityNote = "选择任一品质后，桌面端会继续同时更新 Wi-Fi 与移动网络两套码率设置。",
        ) {
            JvmSettingActionEntryGrid(
                actionEntries = TranscodeAudioBitRateType.entries.map { quality ->
                    JvmSettingActionEntry(
                        icon = Res.drawable.volume_up_24px,
                        kicker = quality.kickerText(),
                        title = quality.audioBitRateStr,
                        description = quality.descriptionText(),
                        selected = selectedQuality == quality,
                        status = "${quality.levelText()} · ${quality.audioBitRate}",
                        role = Role.RadioButton,
                        onClick = {
                            coroutineScope.launch {
                                streamingQualityViewModel.updateWifiNetworkAudioBitRate(quality)
                                streamingQualityViewModel.updateMobileNetworkAudioBitRate(quality)
                            }
                        },
                    )
                },
                arrangement = JvmSettingActionGridArrangement.Horizontal,
            )
        }

        JvmSettingSection(
            title = transcodeFormatTitle,
            subtitle = "格式列表来自服务端支持项，并补齐客户端可显示的默认格式。",
            badge = "服务端能力",
            titleMinWidth = 240.dp,
            contentContainerEnabled = false,
        ) {
            if (streamingQualityViewModel.transcodeAudioBitRateType.isEmpty()) {
                JvmStreamingQualityEmptyState(text = "正在读取服务端支持的转码格式…")
            } else {
                JvmSettingActionEntryGrid(
                    actionEntries = streamingQualityViewModel.transcodeAudioBitRateType.map { format ->
                        val targetFormat = format.targetFormat
                        val title = format.name.ifBlank { targetFormat.uppercase() }
                        JvmSettingActionEntry(
                            icon = Res.drawable.settings_voice_24px,
                            kicker = targetFormat.formatKickerText(),
                            title = title,
                            description = targetFormat.formatDescriptionText(),
                            selected = streamingQualityViewModel.transcodeFormat == targetFormat,
                            status = "targetFormat · $targetFormat",
                            role = Role.RadioButton,
                            onClick = {
                                coroutineScope.launch {
                                    streamingQualityViewModel.updateTranscodeFormat(targetFormat)
                                }
                            },
                        )
                    },
                    arrangement = JvmSettingActionGridArrangement.Horizontal,
                )
            }
        }
    }
}

@Composable
private fun JvmStreamingQualityEmptyState(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
        )
    ) {
        Text(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun TranscodeAudioBitRateType.kickerText(): String {
    return when (this) {
        TranscodeAudioBitRateType.LOSSLESS -> "原始音频"
        TranscodeAudioBitRateType.HIGHEST -> "高质量"
        TranscodeAudioBitRateType.HIGH -> "均衡"
        TranscodeAudioBitRateType.MEDIUM -> "标准"
        TranscodeAudioBitRateType.LOW -> "省流量"
    }
}

private fun TranscodeAudioBitRateType.descriptionText(): String {
    return when (this) {
        TranscodeAudioBitRateType.LOSSLESS -> "尽量保留服务端提供的原始音频流。"
        TranscodeAudioBitRateType.HIGHEST -> "更接近原曲听感，适合稳定网络。"
        TranscodeAudioBitRateType.HIGH -> "兼顾音质与加载速度，桌面端常用。"
        TranscodeAudioBitRateType.MEDIUM -> "默认转码码率，适合多数服务端。"
        TranscodeAudioBitRateType.LOW -> "弱网下更快开始播放，带宽占用更低。"
    }
}

private fun TranscodeAudioBitRateType.levelText(): String {
    return when (this) {
        TranscodeAudioBitRateType.LOSSLESS -> "Raw"
        TranscodeAudioBitRateType.HIGHEST -> "Highest"
        TranscodeAudioBitRateType.HIGH -> "High"
        TranscodeAudioBitRateType.MEDIUM -> "Medium"
        TranscodeAudioBitRateType.LOW -> "Low"
    }
}

private fun String.formatKickerText(): String {
    return when (lowercase()) {
        "mp3" -> "兼容优先"
        "aac" -> "压缩效率"
        else -> "服务端格式"
    }
}

private fun String.formatDescriptionText(): String {
    return when (lowercase()) {
        "mp3" -> "兼容性强，适合跨服务端、跨设备播放。"
        "aac" -> "在相近码率下保持更好的细节与体积平衡。"
        else -> "使用服务端返回的转码格式进行在线播放。"
    }
}
