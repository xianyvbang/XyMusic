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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.common.enums.TranscodeAudioBitRateType
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.StreamingQualityViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.online_music_quality
import xymusic_kmp.composeapp.generated.resources.transcoding_format

private val QualityOptionWidth = 168.dp
private val QualityOptionHeight = 176.dp
private val FormatOptionWidth = 220.dp
private val FormatOptionHeight = 164.dp

@OptIn(ExperimentalLayoutApi::class)
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

    JvmSettingPageScaffold(contentMaxWidth = 1060.dp) {
        JvmSettingPageHeader(
            title = pageTitle,
            description = "选择桌面端播放时使用的在线音频品质和服务端转码格式。当前桌面端使用一组播放品质设置，并同步应用到 Wi-Fi 与移动网络。",
        ) {
            JvmStreamingQualityStatusCard(
                modifier = Modifier.widthIn(min = 248.dp),
                selectedQuality = selectedQuality.audioBitRateStr,
                selectedFormat = selectedFormatLabel,
            )
        }

        JvmSettingSection(
            title = "播放品质",
            subtitle = "桌面端保持一组品质选择，写入时同步更新 Wi-Fi 与移动网络码率。",
            badge = "当前：全网络同步",
            titleMinWidth = 240.dp,
            contentContainerEnabled = false,
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            ) {
                TranscodeAudioBitRateType.entries.forEach { quality ->
                    JvmStreamingQualityOptionCard(
                        modifier = Modifier
                            .width(QualityOptionWidth),
                        kicker = quality.kickerText(),
                        title = quality.audioBitRateStr,
                        description = quality.descriptionText(),
                        footLabel = quality.levelText(),
                        footValue = quality.audioBitRate.toString(),
                        selected = selectedQuality == quality,
                        cardHeight = QualityOptionHeight,
                        onClick = {
                            coroutineScope.launch {
                                streamingQualityViewModel.updateWifiNetworkAudioBitRate(quality)
                                streamingQualityViewModel.updateMobileNetworkAudioBitRate(quality)
                            }
                        }
                    )
                }
            }

            JvmStreamingQualityNote(
                text = "选择任一品质后，桌面端会继续同时更新 Wi-Fi 与移动网络两套码率设置。"
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
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                ) {
                    streamingQualityViewModel.transcodeAudioBitRateType.forEach { format ->
                        val targetFormat = format.targetFormat
                        val title = format.name.ifBlank { targetFormat.uppercase() }
                        JvmStreamingQualityOptionCard(
                            modifier = Modifier
                                .width(FormatOptionWidth),
                            kicker = targetFormat.formatKickerText(),
                            title = title,
                            description = targetFormat.formatDescriptionText(),
                            footLabel = "targetFormat",
                            footValue = targetFormat,
                            selected = streamingQualityViewModel.transcodeFormat == targetFormat,
                            chip = title,
                            cardHeight = FormatOptionHeight,
                            onClick = {
                                coroutineScope.launch {
                                    streamingQualityViewModel.updateTranscodeFormat(targetFormat)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JvmStreamingQualityStatusCard(
    modifier: Modifier = Modifier,
    selectedQuality: String,
    selectedFormat: String,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            JvmStreamingQualityStatusRow(label = "播放品质", value = selectedQuality)
            JvmStreamingQualityStatusRow(label = "转码格式", value = selectedFormat)
            JvmStreamingQualityStatusRow(label = "应用范围", value = "全网络")
        }
    }
}

@Composable
private fun JvmStreamingQualityStatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun JvmStreamingQualityOptionCard(
    modifier: Modifier = Modifier,
    kicker: String,
    title: String,
    description: String,
    footLabel: String,
    footValue: String,
    selected: Boolean,
    chip: String? = null,
    cardHeight: Dp,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = RoundedCornerShape(XyTheme.dimens.corner)
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = when {
        selected -> colorScheme.primary.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.18f else 0.10f)
        hovered -> colorScheme.surfaceContainerHigh
        else -> colorScheme.surfaceContainerLowest
    }
    val borderColor = if (selected) {
        colorScheme.primary.copy(alpha = 0.72f)
    } else {
        colorScheme.onSurface.copy(alpha = if (hovered) 0.16f else 0.10f)
    }

    Box(
        modifier = modifier
            .height(cardHeight)
            .clip(shape)
            .background(containerColor)
            .border(BorderStroke(1.dp, borderColor), shape)
            .pointerHoverIcon(PointerIcon.Hand)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(XyTheme.dimens.outerHorizontalPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (chip == null) {
                    JvmStreamingQualityMeter(active = selected || hovered)
                } else {
                    JvmStreamingQualityChip(text = chip)
                }
                Text(
                    text = kicker,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                modifier = Modifier.padding(
                    top = XyTheme.dimens.outerHorizontalPadding + XyTheme.dimens.outerVerticalPadding / 2
                ),
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.padding(top = XyTheme.dimens.outerVerticalPadding),
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = XyTheme.dimens.outerVerticalPadding * 2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = footLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(start = XyTheme.dimens.outerVerticalPadding),
                    text = footValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.check_24px),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun JvmStreamingQualityMeter(active: Boolean) {
    val barColor = if (active) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
    }
    Row(
        modifier = Modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(6.dp, 10.dp, 14.dp).forEach { height ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height)
                    .background(barColor, CircleShape)
            )
        }
    }
}

@Composable
private fun JvmStreamingQualityChip(text: String) {
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
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.outerVerticalPadding / 2
            ),
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun JvmStreamingQualityNote(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.contentPadding
            ),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "i",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
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
