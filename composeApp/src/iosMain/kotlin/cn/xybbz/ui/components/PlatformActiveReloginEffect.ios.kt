package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import cn.xybbz.api.client.DataSourceManager

@Composable
actual fun PlatformActiveReloginEffect(dataSourceManager: DataSourceManager) {
    LifecycleEffect(
        onPause = dataSourceManager::markAppInactive,
        onResume = dataSourceManager::requestReloginOnAppActive
    )
}
