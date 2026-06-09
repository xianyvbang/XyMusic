package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Composable
internal expect fun rememberFileKitDialogSettings(title: String): FileKitDialogSettings
