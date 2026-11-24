package cn.xybbz.download.core

// Android 下载落盘走 MediaStore，暂不支持自定义下载目录。
internal actual fun platformSupportsCustomDownloadDirectory(): Boolean = false

// Android 暂不支持自定义下载目录，统一回落到平台默认目录。
internal actual fun platformCheckDownloadDirectory(path: String): DownloadDirectoryIssue? =
    DownloadDirectoryIssue.Unsupported
