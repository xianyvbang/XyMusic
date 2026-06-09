package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.Log
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

internal interface BackgroundImagePicker {
    fun pickImage()
}

@Composable
internal fun rememberBackgroundImagePicker(
    onImagePicked: (String?) -> Unit
): BackgroundImagePicker {
    val currentOnImagePicked = rememberUpdatedState(onImagePicked)
    val dialogSettings = rememberFileKitDialogSettings("选择背景图片")
    val imagePickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        dialogSettings = dialogSettings
    ) { selectedFile ->
        selectedFile?.let { file ->
            currentOnImagePicked.value(file.absolutePath())
        }
    }

    return remember(imagePickerLauncher) {
        object : BackgroundImagePicker {
            override fun pickImage() {
                runCatching {
                    imagePickerLauncher.launch()
                }.onFailure { error ->
                    Log.e(Constants.LOG_ERROR_PREFIX, "打开背景图片选择器失败", error)
                }
            }
        }
    }
}
