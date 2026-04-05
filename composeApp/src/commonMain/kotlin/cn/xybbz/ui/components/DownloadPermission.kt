package cn.xybbz.ui.components

import androidx.compose.runtime.Composable

fun interface DownloadPermissionLauncher {
    fun launchMultiplePermissionRequest()
}

@Composable
expect fun downloadPermission(
    ifDownloadApk: Boolean = false,
    onPermissionResult: (Map<String, Boolean>) -> Unit = {}
): DownloadPermissionLauncher

