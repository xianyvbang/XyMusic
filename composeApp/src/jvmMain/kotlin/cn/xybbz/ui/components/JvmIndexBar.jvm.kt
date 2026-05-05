package cn.xybbz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val JvmIndexBarWidth = 36.dp
private val JvmIndexBarItemHeight = 20.dp
private val JvmIndexBarBubbleSize = 70.dp
private val JvmIndexBarBubbleGap = 8.dp
private val JvmIndexBarVerticalPadding = 10.dp

@Composable
internal fun JvmIndexBar(
    modifier: Modifier = Modifier,
    chars: List<Char> = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray().asList(),
    onSelect: (Char) -> Unit = {},
    touchColor: Color = MaterialTheme.colorScheme.secondary,
    charText: @Composable (String, String?) -> Unit = { charText, selectChar ->
        JvmIndexBarCharText(text = charText) { selectChar }
    },
    selectText: @Composable (String) -> Unit = {
        JvmIndexBarSelectText(text = it)
    },
    content: @Composable BoxScope.() -> Unit,
) {
    var hoveredChar by remember { mutableStateOf<Char?>(null) }
    val hoveredIndex = chars.indexOf(hoveredChar)
    val selectedBubbleOffset = selectedBubbleOffset(hoveredIndex)

    Box(modifier = modifier) {
        content()

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(JvmIndexBarBubbleSize + JvmIndexBarBubbleGap + JvmIndexBarWidth)
                .height(JvmIndexBarItemHeight * chars.size + JvmIndexBarVerticalPadding * 2),
        ) {
            hoveredChar?.let { char ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(y = selectedBubbleOffset),
                ) {
                    selectText(char.toString())
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        if (hoveredChar != null) {
                            touchColor.copy(alpha = 0.12f)
                        } else {
                            Color.Transparent
                        }
                    )
                    .padding(vertical = JvmIndexBarVerticalPadding),
            ) {
                chars.forEach { char ->
                    JvmIndexBarItem(
                        char = char,
                        selected = hoveredChar == char,
                        onHover = { hoveredChar = char },
                        onHoverExit = {
                            if (hoveredChar == char) {
                                hoveredChar = null
                            }
                        },
                        onClick = {
                            hoveredChar = char
                            onSelect(char)
                        },
                        charText = charText,
                    )
                }
            }
        }
    }
}

@Composable
private fun JvmIndexBarCharText(
    text: String,
    onSelectChar: () -> String?,
) {
    Box(
        modifier = Modifier
            .width(JvmIndexBarWidth)
            .height(JvmIndexBarItemHeight)
            .clip(CircleShape)
            .background(if (onSelectChar() == text) Color(0xff3b82f6) else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TextStyle.Default,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun JvmIndexBarSelectText(
    text: String,
) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .background(Color(0xff3b82f6).copy(alpha = 0.6f), RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text)
    }
}

@Composable
private fun JvmIndexBarItem(
    char: Char,
    selected: Boolean,
    onHover: () -> Unit,
    onHoverExit: () -> Unit,
    onClick: () -> Unit,
    charText: @Composable (String, String?) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(hovered) {
        if (hovered) {
            onHover()
        } else {
            onHoverExit()
        }
    }

    Box(
        modifier = Modifier
            .width(JvmIndexBarWidth)
            .height(JvmIndexBarItemHeight)
            .hoverable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        charText(char.toString(), if (selected) char.toString() else null)
    }
}

private fun selectedBubbleOffset(selectedIndex: Int): Dp {
    if (selectedIndex < 0) return 0.dp

    return JvmIndexBarVerticalPadding +
        JvmIndexBarItemHeight * selectedIndex +
        (JvmIndexBarItemHeight - JvmIndexBarBubbleSize) / 2
}
