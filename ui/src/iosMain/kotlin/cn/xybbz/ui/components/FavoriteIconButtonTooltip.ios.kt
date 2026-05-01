package cn.xybbz.ui.components

import androidx.compose.runtime.Composable

@Composable
internal actual fun FavoriteIconButtonTooltip(
    tooltip: String,
    content: @Composable () -> Unit,
) {
    content()
}
