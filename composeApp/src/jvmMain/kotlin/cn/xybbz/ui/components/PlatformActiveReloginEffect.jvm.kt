package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cn.xybbz.startup.SessionCoordinator
import cn.xybbz.ui.windows.LocalDesktopWindowFrameState

@Composable
actual fun PlatformActiveReloginEffect(sessionCoordinator: SessionCoordinator) {
    val isFocused = LocalDesktopWindowFrameState.current.isFocused

    LaunchedEffect(sessionCoordinator, isFocused) {
        if (isFocused) {
            sessionCoordinator.requestReloginOnAppActive()
        } else {
            sessionCoordinator.markAppInactive()
        }
    }
}
