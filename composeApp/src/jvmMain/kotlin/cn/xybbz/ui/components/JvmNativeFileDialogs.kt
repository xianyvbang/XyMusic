package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cn.xybbz.ui.windows.LocalDesktopParentWindow
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import java.io.File

/**
 * 创建 JVM 原生文件选择器配置。
 *
 * @param title 系统文件选择器标题。
 * @return 带有主窗口 parent 的 FileKit 弹窗配置，避免弹窗落到应用窗口后面。
 */
@Composable
internal fun rememberJvmFileKitDialogSettings(title: String): FileKitDialogSettings {
    // FileKit 的 JVM 原生弹窗需要主窗口引用才能正确绑定层级。
    val parentWindow = LocalDesktopParentWindow.current

    return remember(title, parentWindow) {
        FileKitDialogSettings(
            title = title,
            parentWindow = parentWindow,
        )
    }
}

/**
 * 将存在的本地目录转换成 FileKit 平台文件。
 *
 * @return 只有当前 File 是已存在目录时才返回 PlatformFile，否则返回 null 让 FileKit 使用默认位置。
 */
internal fun File.toExistingPlatformDirectoryOrNull(): PlatformFile? {
    // 初始目录无效时不要传给原生选择器，避免不同系统出现异常或定位失败。
    return takeIf { it.exists() && it.isDirectory }?.absolutePath?.let(::PlatformFile)
}
