package cn.xybbz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.desktop.DesktopTheme
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.theme.XyTheme

@Composable
fun SidebarVerticalScrollbar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    adapter: ScrollbarAdapter,
) {
    CompositionLocalProvider(
        LocalScrollbarStyle provides ScrollbarStyle(
            minimalHeight = 16.dp,
            thickness = 8.dp,
            shape = MaterialTheme.shapes.small,
            hoverDurationMillis = 300,
            unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f)
        ),
        content = {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = modifier,
            ) {
                VerticalScrollbar(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(6.dp)
                        .padding(vertical = XyTheme.dimens.outerVerticalPadding),
                    adapter = adapter,
                )
            }
        }
    )

}
