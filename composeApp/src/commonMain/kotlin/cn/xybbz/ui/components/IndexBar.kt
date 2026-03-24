package cn.xybbz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun IndexBar(
    modifier: Modifier = Modifier,
    chars: List<Char> = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray().asList(),
    onSelect: (Char) -> Unit = {},
    touchColor: Color = MaterialTheme.colorScheme.secondary,
    charText: @Composable (String, String?) -> Unit = { charText, selectChar ->
        DefaultIndexBarCharText(text = charText) { selectChar }
    },
    selectText: @Composable RowScope.(String) -> Unit = {
        DefaultIndexBarSelectText(text = it)
    },
    content: @Composable BoxScope.() -> Unit
) {
    var isTouch by remember { mutableStateOf(false) }
    var selectChar by remember { mutableStateOf<Char?>(null) }
    var barSize by remember { mutableIntStateOf(0) }

    fun updateSelection(y: Float) {
        if (barSize == 0 || chars.isEmpty()) return

        val itemHeight = barSize.toFloat() / chars.size
        val index = (y / itemHeight)
            .toInt()
            .coerceIn(0, chars.lastIndex)
        val char = chars[index]

        if (char != selectChar) {
            selectChar = char
            onSelect(char)
        }
    }

    Box(modifier = modifier) {
        content()

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectChar != null) {
                selectText(selectChar.toString())
            }

            Column(
                modifier = Modifier
                    .background(if (isTouch) touchColor.copy(alpha = 0.12f) else Color.Transparent)
                    .padding(vertical = 16.dp)
                    .onSizeChanged {
                        barSize = it.height
                    }
                    .pointerInput(chars, barSize) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            isTouch = true
                            updateSelection(down.position.y)
                            down.consume()

                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                updateSelection(change.position.y)
                                if (!change.isConsumed) {
                                    change.consume()
                                }
                            } while (event.changes.any { it.pressed })

                            isTouch = false
                            selectChar = null
                        }
                    },
                verticalArrangement = Arrangement.Center
            ) {
                chars.forEach { char ->
                    charText(char.toString(), selectChar?.toString())
                }
            }
        }
    }
}

@Composable
private fun DefaultIndexBarCharText(
    text: String,
    onSelectChar: () -> String?
) {
    Column(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .clip(CircleShape)
            .background(if (onSelectChar() == text) Color(0xff3b82f6) else Color.Transparent),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            style = TextStyle.Default,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DefaultIndexBarSelectText(
    text: String
) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .background(Color(0xff3b82f6).copy(alpha = 0.6f), RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}
