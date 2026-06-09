package cn.xybbz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.theme.XyTheme

private val JvmScrollbarThickness: @Composable () -> Dp =
    @Composable { XyTheme.dimens.outerHorizontalPadding / 2 }
val JvmHorizontalScrollbarBottomPadding: @Composable () -> Dp =
    { XyTheme.dimens.outerHorizontalPadding }

@Composable
fun SidebarVerticalScrollbar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    adapter: ScrollbarAdapter,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()

    JvmScrollbarStyleProvider {
        AnimatedVisibility(
            visible = visible || isDragged,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = modifier.padding(bottom = XyTheme.dimens.snackBarPlayerHeight)
                .padding(end = XyTheme.dimens.outerVerticalPadding),
        ) {
            VerticalScrollbar(
                modifier = Modifier.fillMaxHeight(),
                adapter = adapter,
                interactionSource = interactionSource,
            )
        }
    }
}

@Composable
fun JvmHorizontalScrollbar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    adapter: ScrollbarAdapter,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()

    JvmScrollbarStyleProvider {
        AnimatedVisibility(
            visible = visible || isDragged,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = modifier,
        ) {
            HorizontalScrollbar(
                modifier = Modifier.fillMaxWidth(),
                adapter = adapter,
                interactionSource = interactionSource,
            )
        }
    }
}

@Composable
private fun JvmScrollbarStyleProvider(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalScrollbarStyle provides ScrollbarStyle(
            minimalHeight = 16.dp,
            thickness = JvmScrollbarThickness(),
            shape = MaterialTheme.shapes.small,
            hoverDurationMillis = 300,
            unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
        ),
    ) {
        content()
    }
}
