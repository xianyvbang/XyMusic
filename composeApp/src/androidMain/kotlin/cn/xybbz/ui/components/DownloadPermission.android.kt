package cn.xybbz.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun downloadPermission(
    ifDownloadApk: Boolean,
    onPermissionResult: (Map<String, Boolean>) -> Unit
): DownloadPermissionLauncher {
    val permissionsToRequest = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ifDownloadApk) {
        permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionState = if (permissionsToRequest.isNotEmpty()) {
        rememberMultiplePermissionsState(
            permissions = permissionsToRequest,
            onPermissionsResult = onPermissionResult
        )
    } else {
        null
    }

    return remember(permissionState, onPermissionResult) {
        DownloadPermissionLauncher {
            if (permissionState == null) {
                onPermissionResult(emptyMap())
            } else {
                permissionState.launchMultiplePermissionRequest()
            }
        }
    }
}
