package cn.xybbz.ui.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import cn.xybbz.ui.theme.XyTheme
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.ui.generated.resources.Res
import xymusic_kmp.ui.generated.resources.close_window
import xymusic_kmp.ui.generated.resources.maximize_window
import xymusic_kmp.ui.generated.resources.minimize_window
import xymusic_kmp.ui.generated.resources.restore_window

@Composable
internal fun RowScope.DefaultDesktopWindowTitleBarBack(
    isMaximized: Boolean,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    onClose: () -> Unit,
    showMinimizeButton: Boolean,
    showMaximizeButton: Boolean,
    showCloseButton: Boolean,
    minimizeIcon: WindowControlIcon,
    maximizeIcon: WindowControlIcon,
    restoreIcon: WindowControlIcon,
    closeIcon: WindowControlIcon,
    beforeWindowControls: (@Composable RowScope.() -> Unit)?,
    afterWindowControls: (@Composable RowScope.() -> Unit)?,
    onControlButtonBoundsChanged: (WindowControlType, Rect) -> Unit,
) {
    beforeWindowControls?.invoke(this)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showMinimizeButton) {
            DesktopWindowControlTooltipBox(
                tooltip = stringResource(Res.string.minimize_window)
            ) {
                DesktopWindowControlButton(
                    controlType = WindowControlType.Minimize,
                    icon = minimizeIcon,
                    onClick = onMinimize,
                    onBoundsChanged = onControlButtonBoundsChanged,
                )
            }
        }

        if (showMaximizeButton) {
            val controlType = if (isMaximized) {
                WindowControlType.Restore
            } else {
                WindowControlType.Maximize
            }
            DesktopWindowControlTooltipBox(
                tooltip = stringResource(
                    if (isMaximized) {
                        Res.string.restore_window
                    } else {
                        Res.string.maximize_window
                    }
                )
            ) {
                DesktopWindowControlButton(
                    controlType = controlType,
                    icon = if (isMaximized) restoreIcon else maximizeIcon,
                    onClick = onToggleMaximize,
                    onBoundsChanged = onControlButtonBoundsChanged,
                )
            }
        }

        if (showCloseButton) {
            DesktopWindowControlTooltipBox(
                tooltip = stringResource(Res.string.close_window)
            ) {
                DesktopWindowControlButton(
                    controlType = WindowControlType.Close,
                    icon = closeIcon,
                    onClick = onClose,
                    onBoundsChanged = onControlButtonBoundsChanged,
                )
            }
        }
    }

    afterWindowControls?.invoke(this)
}
