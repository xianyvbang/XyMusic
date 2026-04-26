package cn.xybbz.ui.windows

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

object WindowControlButtonIcons {
    @Composable
    fun Minimize(tint: Color, modifier: Modifier = Modifier) {
        WindowControlGlyph(
            controlType = WindowControlType.Minimize,
            tint = tint,
            modifier = modifier
        )
    }

    @Composable
    fun Maximize(tint: Color, modifier: Modifier = Modifier) {
        WindowControlGlyph(
            controlType = WindowControlType.Maximize,
            tint = tint,
            modifier = modifier
        )
    }

    @Composable
    fun Restore(tint: Color, modifier: Modifier = Modifier) {
        WindowControlGlyph(
            controlType = WindowControlType.Restore,
            tint = tint,
            modifier = modifier
        )
    }

    @Composable
    fun Close(tint: Color, modifier: Modifier = Modifier) {
        WindowControlGlyph(
            controlType = WindowControlType.Close,
            tint = tint,
            modifier = modifier
        )
    }
}

@Composable
private fun WindowControlGlyph(
    controlType: WindowControlType,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.4.dp.toPx()
        val inset = 1.5.dp.toPx()
        val left = inset
        val top = inset
        val right = size.width - inset
        val bottom = size.height - inset

        when (controlType) {
            WindowControlType.Minimize -> {
                drawLine(
                    color = tint,
                    start = Offset(left, bottom - inset),
                    end = Offset(right, bottom - inset),
                    strokeWidth = strokeWidth
                )
            }

            WindowControlType.Maximize -> {
                drawRect(
                    color = tint,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = strokeWidth)
                )
            }

            WindowControlType.Restore -> {
                val shift = 2.dp.toPx()
                drawRect(
                    color = tint,
                    topLeft = Offset(left + shift, top),
                    size = Size(right - left - shift, bottom - top - shift),
                    style = Stroke(width = strokeWidth)
                )
                drawRect(
                    color = tint,
                    topLeft = Offset(left, top + shift),
                    size = Size(right - left - shift, bottom - top - shift),
                    style = Stroke(width = strokeWidth)
                )
            }

            WindowControlType.Close -> {
                drawLine(
                    color = tint,
                    start = Offset(left, top),
                    end = Offset(right, bottom),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = tint,
                    start = Offset(right, top),
                    end = Offset(left, bottom),
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}
