package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalTextApi::class)
@Composable
fun textToBitmap(name: String, url: String? = null): Any {
    if (!url.isNullOrBlank()) return url

    val letter = remember(name) {
        name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }
    val textMeasurer = rememberTextMeasurer()

    return remember(letter, textMeasurer) {
        InitialLetterPainter(
            letter = letter,
            textMeasurer = textMeasurer
        )
    }
}

private class InitialLetterPainter(
    private val letter: String,
    private val textMeasurer: TextMeasurer,
) : Painter() {
    private val textStyle = TextStyle(
        color = Color.White,
        fontSize = 64.sp,
        fontWeight = FontWeight.Bold
    )

    override val intrinsicSize: Size = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawRect(
            color = Color(0x6440704A),
            size = size
        )

        val textLayoutResult = textMeasurer.measure(
            text = AnnotatedString(letter),
            style = textStyle
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x = (size.width - textLayoutResult.size.width) / 2f,
                y = (size.height - textLayoutResult.size.height) / 2f
            )
        )
    }
}
