package cn.xybbz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyImage
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.background_image

/**
 * 背景图遮罩不透明度。
 *
 * 遮罩用主题背景色压住图片，既保留背景图的氛围，又让其上的文字始终有稳定的对比底色。
 * 深色主题文字是浅色，遇到偏亮的图片最容易被冲淡，所以遮罩要更实一些。
 * 超过 0.9 后背景图基本只剩色块，失去装饰意义，因此上限控制在 0.88。
 */
internal const val BackgroundImageScrimAlphaDark = 0.88f
internal const val BackgroundImageScrimAlphaLight = 0.84f

/**
 * 主壳背景层：背景图 + 统一遮罩。
 *
 * 背景图只在主壳绘制一次，页面内不再重复绘制。这样图片不会被内容区二次裁剪，
 * 所有页面也能共用同一层遮罩，文字对比度不随用户选的图片明暗而失控。
 */
@Composable
internal fun MainScreenBackgroundLayer(modifier: Modifier = Modifier) {
    val backgroundImageUri = XyTheme.brash.backgroundImageUri

    Box(modifier = modifier) {
        XyImage(
            modifier = Modifier.fillMaxSize(),
            model = backgroundImageUri,
            contentDescription = stringResource(Res.string.background_image),
        )
        if (!backgroundImageUri.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.background.copy(
                            alpha = if (XyTheme.configs.isDarkTheme) {
                                BackgroundImageScrimAlphaDark
                            } else {
                                BackgroundImageScrimAlphaLight
                            }
                        )
                    )
            )
        }
    }
}
