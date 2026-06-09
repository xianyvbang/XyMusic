package cn.xybbz.ui.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.theme.XyTheme

object DesktopWindowTitleBarDefaults {
    val Height: Dp
        @Composable
        get() = XyTheme.dimens.itemHeight * 1.3f

    val WindowControlButtonWidth: Dp = 42.dp

    val WindowControlButtonHeight: Dp = 34.dp

    val WindowControlIconSize: Dp = 12.dp

    const val TooltipDelayMillis: Long = 500L
}
