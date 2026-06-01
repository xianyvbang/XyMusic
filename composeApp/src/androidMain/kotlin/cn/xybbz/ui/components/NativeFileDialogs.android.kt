package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Composable
internal actual fun rememberFileKitDialogSettings(title: String): FileKitDialogSettings {
    return remember(title) {
        FileKitDialogSettings.createDefault()
    }
}
