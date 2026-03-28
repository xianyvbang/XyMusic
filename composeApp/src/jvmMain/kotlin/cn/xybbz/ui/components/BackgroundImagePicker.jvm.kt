package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
internal actual fun rememberBackgroundImagePicker(
    onImagePicked: (String?) -> Unit
): BackgroundImagePicker {
    val currentOnImagePicked = rememberUpdatedState(onImagePicked)

    return remember {
        object : BackgroundImagePicker {
            override fun pickImage() {
                val chooser = JFileChooser(defaultDirectory()).apply {
                    dialogTitle = "选择背景图片"
                    fileSelectionMode = JFileChooser.FILES_ONLY
                    isAcceptAllFileFilterUsed = true
                    addChoosableFileFilter(
                        FileNameExtensionFilter(
                            "Image Files",
                            "png",
                            "jpg",
                            "jpeg",
                            "webp",
                            "gif",
                            "bmp"
                        )
                    )
                }

                if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                    return
                }

                currentOnImagePicked.value(chooser.selectedFile?.absolutePath)
            }
        }
    }
}

private fun defaultDirectory(): File {
    val userHome = File(System.getProperty("user.home") ?: ".")
    val picturesDir = File(userHome, "Pictures")
    return if (picturesDir.exists()) picturesDir else userHome
}
