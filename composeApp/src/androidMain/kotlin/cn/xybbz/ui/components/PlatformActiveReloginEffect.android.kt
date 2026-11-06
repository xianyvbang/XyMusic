package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import cn.xybbz.startup.SessionCoordinator

@Composable
actual fun PlatformActiveReloginEffect(sessionCoordinator: SessionCoordinator) {
    LifecycleEffect(
        onPause = sessionCoordinator::markAppInactive,
        onResume = sessionCoordinator::requestReloginOnAppActive
    )
}
