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
import cn.xybbz.ui.components.displayAudioBitRateText
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.StreamingQualityViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.*
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.online_music_quality
import xymusic.composeapp.generated.resources.settings_voice_24px
import xymusic.composeapp.generated.resources.transcoding_format
import xymusic.composeapp.generated.resources.volume_up_24px
import cn.xybbz.ui.components.JvmSettingActionGrid as JvmSettingActionEntryGrid

@Composable
fun JvmStreamingQualityScreen(
    streamingQualityViewModel: StreamingQualityViewModel = koinViewModel<StreamingQualityViewModel>(),
) {

    val coroutineScope = rememberCoroutineScope()
    val pageTitle = stringResource(Res.string.online_music_quality)
    val transcodeFormatTitle = stringResource(Res.string.transcoding_format)
    val selectedQuality = streamingQualityViewModel.wifiNetworkAudioBitRate
    val selectedQualityLabel = selectedQuality.displayAudioBitRateText()
    val selectedFormatLabel = streamingQualityViewModel.transcodeAudioBitRateType
        .firstOrNull { it.targetFormat == streamingQualityViewModel.transcodeFormat }
        ?.name
        ?: streamingQualityViewModel.transcodeFormat.uppercase()

    JvmSettingPageScaffold() {
        JvmSettingPageHeader(
            title = pageTitle,
            description = stringResource(Res.string.jvm_streaming_quality_screen_text_01),
        ) {
            JvmSettingStatusCard(
                items = listOf(
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_streaming_quality_screen_text_02), value = selectedQualityLabel),
                    JvmSettingStatusCardItem(label = stringResource(Res.string.transcoding_format), value = selectedFormatLabel),
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_streaming_quality_screen_text_03), value = stringResource(Res.string.jvm_streaming_quality_screen_text_04)),
                )
            )
        }

        JvmSettingSection(
            title = stringResource(Res.string.jvm_streaming_quality_screen_text_02),
            subtitle = stringResource(Res.string.jvm_streaming_quality_screen_text_05),
            badge = stringResource(Res.string.jvm_streaming_quality_screen_text_06),
            titleMinWidth = 240.dp,
            contentContainerEnabled = false,
            qualityNote = stringResource(Res.string.jvm_streaming_quality_screen_text_07),
        ) {
            JvmSettingActionEntryGrid(
                actionEntries = TranscodeAudioBitRateType.entries.map { quality ->
                    JvmSettingActionEntry(
                        icon = Res.drawable.volume_up_24px,
                        kicker = quality.kickerText(),
                        title = quality.displayAudioBitRateText(),
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
            subtitle = stringResource(Res.string.jvm_streaming_quality_screen_text_08),
            badge = stringResource(Res.string.jvm_streaming_quality_screen_text_09),
            titleMinWidth = 240.dp,
            contentContainerEnabled = false,
        ) {
            if (streamingQualityViewModel.transcodeAudioBitRateType.isEmpty()) {
                JvmStreamingQualityEmptyState(text = stringResource(Res.string.jvm_streaming_quality_screen_text_10))
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

@Composable
private fun TranscodeAudioBitRateType.kickerText(): String {
    return when (this) {
        TranscodeAudioBitRateType.LOSSLESS -> stringResource(Res.string.jvm_streaming_quality_screen_text_11)
        TranscodeAudioBitRateType.HIGHEST -> stringResource(Res.string.jvm_streaming_quality_screen_text_12)
        TranscodeAudioBitRateType.HIGH -> stringResource(Res.string.jvm_cache_limit_screen_text_32)
        TranscodeAudioBitRateType.MEDIUM -> stringResource(Res.string.jvm_streaming_quality_screen_text_13)
        TranscodeAudioBitRateType.LOW -> stringResource(Res.string.jvm_streaming_quality_screen_text_14)
    }
}

@Composable
private fun TranscodeAudioBitRateType.descriptionText(): String {
    return when (this) {
        TranscodeAudioBitRateType.LOSSLESS -> stringResource(Res.string.jvm_streaming_quality_screen_text_15)
        TranscodeAudioBitRateType.HIGHEST -> stringResource(Res.string.jvm_streaming_quality_screen_text_16)
        TranscodeAudioBitRateType.HIGH -> stringResource(Res.string.jvm_streaming_quality_screen_text_17)
        TranscodeAudioBitRateType.MEDIUM -> stringResource(Res.string.jvm_streaming_quality_screen_text_18)
        TranscodeAudioBitRateType.LOW -> stringResource(Res.string.jvm_streaming_quality_screen_text_19)
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

@Composable
private fun String.formatKickerText(): String {
    return when (lowercase()) {
        "mp3" -> stringResource(Res.string.jvm_streaming_quality_screen_text_20)
        "aac" -> stringResource(Res.string.jvm_streaming_quality_screen_text_21)
        else -> stringResource(Res.string.jvm_streaming_quality_screen_text_22)
    }
}

@Composable
private fun String.formatDescriptionText(): String {
    return when (lowercase()) {
        "mp3" -> stringResource(Res.string.jvm_streaming_quality_screen_text_23)
        "aac" -> stringResource(Res.string.jvm_streaming_quality_screen_text_24)
        else -> stringResource(Res.string.jvm_streaming_quality_screen_text_25)
    }
}
