package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import cn.xybbz.ui.windows.DesktopTooltipBox

@Composable
internal actual fun FavoriteIconButtonTooltip(
    tooltip: String,
    content: @Composable () -> Unit,
) {
    DesktopTooltipBox(tooltip = tooltip, content = content)
}
