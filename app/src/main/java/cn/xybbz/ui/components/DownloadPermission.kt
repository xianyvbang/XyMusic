package cn.xybbz.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun downloadPermission(onPermissionResult: (Map<String, Boolean>) -> Unit = {}): MultiplePermissionsState? {
    val permissionsToRequest = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
    }
    // 为 Android 9 及以下请求外部存储写入权限
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    return if (permissionsToRequest.isNotEmpty())
        rememberMultiplePermissionsState(
            permissionsToRequest,
            onPermissionsResult = onPermissionResult
        )
    else null
}