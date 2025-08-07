package cn.xybbz.ui.ext

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Modifier.brashColor(
    topVerticalColor: Color = Color(0xFF600015),
    bottomVerticalColor: Color = Color(0xFF04727E)
): Modifier = then(Modifier.drawWithCache {
    onDrawBehind {
        // 绘制竖向渐变
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(topVerticalColor, bottomVerticalColor),
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
    }
}
)