package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.Log
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch

internal interface BackgroundImagePicker {
    fun pickImage()
}

@Composable
internal fun rememberBackgroundImagePicker(
    onImagePicked: (String?) -> Unit
): BackgroundImagePicker {
    val currentOnImagePicked = rememberUpdatedState(onImagePicked)
    val coroutineScope = rememberCoroutineScope()
    val dialogSettings = rememberFileKitDialogSettings("选择背景图片")

    return remember(coroutineScope, dialogSettings) {
        object : BackgroundImagePicker {
            override fun pickImage() {
                coroutineScope.launch {
                    val selectedFile = runCatching {
                        FileKit.openFilePicker(
                            type = FileKitType.Image,
                            dialogSettings = dialogSettings
                        )
                    }.onFailure { error ->
                        Log.e(Constants.LOG_ERROR_PREFIX, "打开背景图片选择器失败", error)
                    }.getOrNull() ?: return@launch

                    currentOnImagePicked.value(selectedFile.absolutePath())
                }
            }
        }
    }
}
