package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun downloadPermission(
    ifDownloadApk: Boolean,
    onPermissionResult: (Map<String, Boolean>) -> Unit
): DownloadPermissionLauncher {
    return remember(onPermissionResult) {
        DownloadPermissionLauncher {
            onPermissionResult(emptyMap())
        }
    }
}
