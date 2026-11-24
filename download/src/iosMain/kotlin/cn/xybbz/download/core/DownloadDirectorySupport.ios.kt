package cn.xybbz.download.core

// iOS 暂不支持自定义下载目录。
internal actual fun platformSupportsCustomDownloadDirectory(): Boolean = false

// iOS 暂不支持自定义下载目录，统一回落到平台默认目录。
internal actual fun platformCheckDownloadDirectory(path: String): DownloadDirectoryIssue? =
    DownloadDirectoryIssue.Unsupported
