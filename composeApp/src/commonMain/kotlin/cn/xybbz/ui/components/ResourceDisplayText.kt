package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import cn.xybbz.common.enums.TranscodeAudioBitRateType
import cn.xybbz.music.CacheUpperLimitOption
import org.jetbrains.compose.resources.stringResource

/**
 * 解析缓存上限选项的展示文案，优先使用可本地化资源。
 */
@Composable
fun CacheUpperLimitOption.displayMessage(): String {
    return messageResource?.let { stringResource(it) } ?: message
}

/**
 * 解析转码码率选项的展示文案，优先使用可本地化资源。
 */
@Composable
fun TranscodeAudioBitRateType.displayAudioBitRateText(): String {
    return audioBitRateResource?.let { stringResource(it) } ?: audioBitRateStr
}
