package cn.xybbz.ui.windows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme

@Composable
internal fun DesktopWindowControlButton(
    controlType: WindowControlType,
    icon: WindowControlIcon,
    onClick: () -> Unit,
    onBoundsChanged: (WindowControlType, Rect) -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(
                width = DesktopWindowTitleBarDefaults.WindowControlButtonWidth,
                height = DesktopWindowTitleBarDefaults.WindowControlButtonHeight
            )
            .onGloballyPositioned { coordinates ->
                onBoundsChanged(controlType, coordinates.boundsInWindow())
            }
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon(
            contentColor,
            Modifier.size(DesktopWindowTitleBarDefaults.WindowControlIconSize)
        )
    }
}
