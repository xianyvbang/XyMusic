package cn.xybbz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.theme.XyTheme

private val JvmScrollbarThickness = 8.dp

@Composable
fun SidebarVerticalScrollbar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    adapter: ScrollbarAdapter,
) {
    CompositionLocalProvider(
        LocalScrollbarStyle provides ScrollbarStyle(
            minimalHeight = 16.dp,
            thickness = JvmScrollbarThickness,
            shape = MaterialTheme.shapes.small,
            hoverDurationMillis = 300,
            unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
        ),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = modifier,
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = XyTheme.dimens.snackBarPlayerHeight)
                    .padding(end = XyTheme.dimens.outerVerticalPadding),
            ) {
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight(),
                    adapter = adapter,
                )
            }
        }
    }
}

@Composable
internal fun JvmHorizontalScrollbar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    adapter: ScrollbarAdapter,
) {
    CompositionLocalProvider(
        LocalScrollbarStyle provides ScrollbarStyle(
            minimalHeight = 16.dp,
            thickness = JvmScrollbarThickness,
            shape = MaterialTheme.shapes.small,
            hoverDurationMillis = 300,
            unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
        ),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = modifier,
        ) {
            HorizontalScrollbar(
                modifier = Modifier.fillMaxWidth(),
                adapter = adapter,
            )
        }
    }
}
