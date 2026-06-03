package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.ui.windows.LocalDesktopWindowFrameState

@Composable
actual fun PlatformActiveReloginEffect(dataSourceManager: DataSourceManager) {
    val isFocused = LocalDesktopWindowFrameState.current.isFocused

    LaunchedEffect(dataSourceManager, isFocused) {
        if (isFocused) {
            dataSourceManager.requestReloginOnAppActive()
        } else {
            dataSourceManager.markAppInactive()
        }
    }
}
