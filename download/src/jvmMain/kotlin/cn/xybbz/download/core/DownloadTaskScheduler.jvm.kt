package cn.xybbz.download.core

import cn.xybbz.platform.ContextWrapper

// JVM 端下载调度先留空，后续真正接桌面后台任务时再补实现。
actual class DownloadTaskScheduler actual constructor(val contextWrapper: ContextWrapper) {
    actual fun enqueue(downloadId: Long) = Unit

    actual fun cancel(downloadId: Long) = Unit
}
