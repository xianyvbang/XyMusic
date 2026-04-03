package cn.xybbz.config.download

import cn.xybbz.download.core.DownloadDispatcherImpl
import cn.xybbz.download.core.DownloadImpl
import cn.xybbz.config.download.core.DownloaderConfig
import cn.xybbz.config.download.core.IDownloader
import cn.xybbz.database.DatabaseClient

class DownLoadManager(
    private val db: DatabaseClient,
    val downloadDispatcher: DownloadDispatcherImpl,
) : IDownloader by DownloadImpl(
    downloadDispatcher,
    applicationContext,
    db
) {
    fun getConfig(): DownloaderConfig{
        return downloadDispatcher.config
    }

}