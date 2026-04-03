package cn.xybbz.download.core

import cn.xybbz.platform.ContextWrapper

// 平台侧真正启动下载任务的入口直接走 expect/actual，commonMain 只关心调度动作本身。
expect class DownloadTaskScheduler(contextWrapper: ContextWrapper) {
    fun enqueue(downloadId: Long)
    fun cancel(downloadId: Long)
}
