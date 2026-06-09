package cn.xybbz.ui.windows

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect

@Composable
fun DesktopWindowControls(
    modifier: Modifier = Modifier,
    frameState: DesktopWindowFrameState = LocalDesktopWindowFrameState.current,
    chromeController: DesktopWindowChromeController = LocalDesktopWindowChromeController.current,
    isMaximized: Boolean = frameState.isMaximized,
    onMinimize: () -> Unit = frameState.onMinimize,
    onToggleMaximize: () -> Unit = frameState.onToggleMaximize,
    onClose: () -> Unit = frameState.onClose,
    showMinimizeButton: Boolean = true,
    showMaximizeButton: Boolean = true,
    showCloseButton: Boolean = true,
) {
    DisposableEffect(chromeController, showMinimizeButton, showMaximizeButton, showCloseButton) {
        onDispose {
            if (showMinimizeButton) {
                chromeController.updateMinimizeButtonBounds(Rect.Zero)
            }
            if (showMaximizeButton) {
                chromeController.updateMaximizeButtonBounds(Rect.Zero)
            }
            if (showCloseButton) {
                chromeController.updateCloseButtonBounds(Rect.Zero)
            }
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DefaultDesktopWindowTitleBarBack(
            isMaximized = isMaximized,
            onMinimize = onMinimize,
            onToggleMaximize = onToggleMaximize,
            onClose = onClose,
            showMinimizeButton = showMinimizeButton,
            showMaximizeButton = showMaximizeButton,
            showCloseButton = showCloseButton,
            minimizeIcon = { tint, iconModifier ->
                WindowControlButtonIcons.Minimize(tint, iconModifier)
            },
            maximizeIcon = { tint, iconModifier ->
                WindowControlButtonIcons.Maximize(tint, iconModifier)
            },
            restoreIcon = { tint, iconModifier ->
                WindowControlButtonIcons.Restore(tint, iconModifier)
            },
            closeIcon = { tint, iconModifier ->
                WindowControlButtonIcons.Close(tint, iconModifier)
            },
            beforeWindowControls = null,
            afterWindowControls = null,
            onControlButtonBoundsChanged = { type, bounds ->
                when (type) {
                    WindowControlType.Minimize -> chromeController.updateMinimizeButtonBounds(bounds)
                    WindowControlType.Maximize,
                    WindowControlType.Restore -> chromeController.updateMaximizeButtonBounds(bounds)

                    WindowControlType.Close -> chromeController.updateCloseButtonBounds(bounds)
                }
            },
        )
    }
}
