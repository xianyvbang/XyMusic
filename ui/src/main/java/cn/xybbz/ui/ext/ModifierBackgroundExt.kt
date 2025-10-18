package cn.xybbz.ui.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import cn.xybbz.ui.theme.LocalXyBackgroundBrash

/**
 * 背景渐变
 * @param [topVerticalColor] 顶部垂直颜色
 * @param [bottomVerticalColor] 底部垂直颜色
 * @param [topGlobalColor] 顶部全局颜色
 * @param [bottomGlobalColor] 底部全局颜色
 * @param [ifOneColor] 是否使用一种颜色
 * @param [ifGlobalBrash] 如果全局颜色
 * @return [Modifier]
 */
@Composable
fun Modifier.brashColor(
    topVerticalColor: Color = Color(0xFF600015),
    bottomVerticalColor: Color = Color(0xFF04727E),
    topGlobalColor: Color? = null,
    bottomGlobalColor: Color? = null,
    ifOneColor: Boolean? = null,
    ifGlobalBrash: Boolean? = null
): Modifier {
    // 读取 CompositionLocal
    val cfg = LocalXyBackgroundBrash.current

    val topColor = topGlobalColor ?: cfg.globalBrash[0]
    val bottomColor = bottomGlobalColor ?: cfg.globalBrash[1]
    val useOne = ifOneColor ?: cfg.ifChangeOneColor
    val useGlobal = ifGlobalBrash ?: cfg.ifGlobalBrash

    val brushColors = if (useGlobal) {
        listOf(topColor, bottomColor)
    } else {
        listOf(topVerticalColor, bottomVerticalColor)
    }
    return this.drawWithCache {
        onDrawBehind {
            // 绘制竖向渐变
            if (!useOne) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = brushColors,
                        startY = 0f,
                        endY = size.height
                    ),
                    blendMode = BlendMode.SrcOver
                )
                // 绘制横向渐变
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent.copy(alpha = 0.1f),
                            Color.Transparent.copy(alpha = 1f)
                        ),
                        startX = 0f,
                        endX = size.width
                    ),
                    blendMode = BlendMode.Screen // 使用混合模式
                )
            } else {
                drawRect(if (useGlobal) topColor else topVerticalColor)
            }
        }
    }
}