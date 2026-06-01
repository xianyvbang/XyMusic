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
import java.io.File

@Composable
internal actual fun rememberBackgroundImagePicker(
    onImagePicked: (String?) -> Unit
): BackgroundImagePicker {
    // 保存最新回调，避免 remember 的 picker 对象持有旧闭包。
    val currentOnImagePicked = rememberUpdatedState(onImagePicked)
    // FileKit 选择器是 suspend API，点击入口保持普通函数，由协程承接。
    val coroutineScope = rememberCoroutineScope()
    // 给系统图片选择器绑定主窗口，确保弹窗在桌面应用前方显示。
    val dialogSettings = rememberJvmFileKitDialogSettings("选择背景图片")

    return remember(coroutineScope, dialogSettings) {
        object : BackgroundImagePicker {
            override fun pickImage() {
                coroutineScope.launch {
                    // 仅允许用户选择应用支持的背景图片格式，取消选择时直接保持当前背景不变。
                    val selectedFile = runCatching {
                        FileKit.openFilePicker(
                            type = FileKitType.File("png", "jpg", "jpeg", "webp", "gif", "bmp"),
                            directory = defaultDirectory().toExistingPlatformDirectoryOrNull(),
                            dialogSettings = dialogSettings,
                        )
                    }.onFailure { error ->
                        Log.e(Constants.LOG_ERROR_PREFIX, "打开背景图片选择器失败", error)
                    }.getOrNull() ?: return@launch

                    // JVM 端业务仍消费绝对路径字符串，避免改动 common 层接口。
                    currentOnImagePicked.value(selectedFile.absolutePath())
                }
            }
        }
    }
}

/**
 * 获取背景图片选择器的默认目录。
 *
 * @return 优先返回用户图片目录，不存在时回退到用户主目录。
 */
private fun defaultDirectory(): File {
    val userHome = File(System.getProperty("user.home") ?: ".")
    val picturesDir = File(userHome, "Pictures")
    return if (picturesDir.exists()) picturesDir else userHome
}
