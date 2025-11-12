package cn.xybbz.config.download.core

import cn.xybbz.localdata.data.download.XyDownload

interface IDownloader {
    suspend fun enqueue(vararg requests: DownloadRequest)
    fun pause(vararg ids: Long)
    fun resume(vararg ids: Long)
    fun cancel(vararg ids: Long)
    fun delete(vararg ids: Long, deleteFile: Boolean = true)
    fun updateConfig(config: DownloaderConfig)
}