package cn.xybbz.download.core

import cn.xybbz.platform.ContextWrapper

// iOS 端下载调度先留空，当前只保证多平台结构完整。
actual class DownloadTaskScheduler actual constructor(contextWrapper: ContextWrapper) {
    actual fun enqueue(downloadId: Long) = Unit

    actual fun cancel(downloadId: Long) = Unit
}
