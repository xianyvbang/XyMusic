package cn.xybbz.config.download

import android.content.Context
import cn.xybbz.config.download.core.DownloadDispatcherImpl
import cn.xybbz.config.download.core.DownloadImpl
import cn.xybbz.config.download.core.DownloaderConfig
import cn.xybbz.config.download.core.IDownloader
import cn.xybbz.localdata.config.DatabaseClient

class DownLoadManager(
    val applicationContext: Context,
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