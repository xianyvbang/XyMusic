package cn.xybbz.ui.ext

import androidx.annotation.FloatRange
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
 * @param [alpha] 颜色透明度
 * @param [topGlobalColor] 顶部全局颜色
 * @param [bottomGlobalColor] 底部全局颜色
 * @param [ifOneColor] 是否使用一种颜色
 * @param [ifGlobalBrash] 如果全局颜色
 * @param [ifShowBackgroundImage] 是否显示背景图片
 * @return [Modifier]
 */
@Composable
fun Modifier.brashColor(
    topVerticalColor: Color = Color(0xFF600015),
    bottomVerticalColor: Color = Color(0xFF04727E),
    @FloatRange(from = 0.0, to = 1.0) alpha: Float? = null,
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
    val useAlpha by remember {
        derivedStateOf {
            alpha ?: if (cfg.backgroundImageUri != null) 0.85f else 1f
        }
    }

    // 使用 animateFloatAsState 实现平滑过渡
    val brashColorAlphaAnimate by animateFloatAsState(
        targetValue = useAlpha,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
    )

    val brushColors by remember {
        derivedStateOf {
            if (useGlobal) {
                listOf(topColor, bottomColor)
            } else {
                listOf(topVerticalColor, bottomVerticalColor)
            }
        }
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
                    alpha = brashColorAlphaAnimate,
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