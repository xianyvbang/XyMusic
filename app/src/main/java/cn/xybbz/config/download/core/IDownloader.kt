package cn.xybbz.config.download.core


interface IDownloader {
    fun initData()
    suspend fun enqueue(vararg requests: DownloadRequest)
    fun pause(vararg ids: Long)
    fun resume(vararg ids: Long)
    fun cancel(vararg ids: Long)
    fun delete(vararg ids: Long, deleteFile: Boolean = true)
    fun updateConfig(config: DownloaderConfig)
    fun addListener(listener: DownloadListener)
    fun removerListener(listener: DownloadListener)
}