package cn.xybbz.ui.windows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.awt.ComposeWindow
import com.sun.jna.Platform

@Composable
fun rememberWindowsWindowChromeController(
    window: ComposeWindow,
    onCloseRequest: () -> Unit,
): DesktopWindowChromeController {
    if (!Platform.isWindows()) {
        return DesktopWindowChromeController.None
    }

    val controller = remember(window) {
        WindowsWindowChromeController(
            window = window,
            onCloseRequest = onCloseRequest
        )
    }

    DisposableEffect(controller) {
        controller.install()
        onDispose {
            controller.dispose()
        }
    }

    return controller
}
