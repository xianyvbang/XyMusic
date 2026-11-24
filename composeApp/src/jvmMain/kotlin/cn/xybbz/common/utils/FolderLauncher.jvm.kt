package cn.xybbz.common.utils

import java.awt.Desktop
import java.io.File

/**
 * 在系统文件管理器中打开指定目录。
 *
 * @param path 目标目录路径；传入的是文件路径时自动取其所在目录。
 * @return 是否成功发起打开操作，调用方可据此给出提示。
 */
internal fun openFolderInSystemFileManager(path: String): Boolean {
    val file = File(path.trim())
    // 传入文件路径时定位到其所在目录，没有父目录则视为无法打开
    val target = if (file.isDirectory) file else file.parentFile ?: return false
    if (!target.exists()) {
        return false
    }
    return runCatching {
        val osName = System.getProperty("os.name").orEmpty().lowercase()
        if (osName.contains("win")) {
            // Windows 下交给 explorer 打开目录，避免部分精简环境 Desktop 不可用
            ProcessBuilder("explorer", target.absolutePath).start()
            true
        } else {
            val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
            if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(target)
                true
            } else {
                false
            }
        }
    }.getOrDefault(false)
}
