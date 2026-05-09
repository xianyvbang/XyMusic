package cn.xybbz.platform

import java.io.File

actual class ContextWrapper {
    val applicationDirectory: File = File(
        System.getProperty("user.dir").orEmpty().ifBlank { "." }
    ).absoluteFile
}
