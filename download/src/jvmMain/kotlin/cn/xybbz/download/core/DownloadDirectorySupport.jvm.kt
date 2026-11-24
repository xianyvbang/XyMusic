package cn.xybbz.download.core

import java.io.File

// JVM 端支持通过系统目录选择器自定义下载目录。
internal actual fun platformSupportsCustomDownloadDirectory(): Boolean = true

// JVM 端基于 java.io.File 判断目录类型、写入权限与可用空间。
internal actual fun platformCheckDownloadDirectory(path: String): DownloadDirectoryIssue? {
    val directory = File(path)

    // 目录被删除或被移除的磁盘（如未挂载的移动硬盘）不再自动重建，直接视为不可用并回落到默认目录。
    if (!directory.exists()) {
        return DownloadDirectoryIssue.NotCreatable
    }
    if (!directory.isDirectory) {
        return DownloadDirectoryIssue.NotDirectory
    }
    if (!directory.canWrite()) {
        return DownloadDirectoryIssue.NotWritable
    }
    if (directory.usableSpace <= 0L) {
        return DownloadDirectoryIssue.NoSpace
    }
    return null
}
