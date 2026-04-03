package cn.xybbz.download

import cn.xybbz.download.core.IDownloader
import cn.xybbz.download.core.DownloadDispatcherImpl
import cn.xybbz.download.core.DownloadImpl
import cn.xybbz.download.core.DownloaderConfig
import cn.xybbz.download.core.HttpClientFactory
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.platform.ContextWrapper

class DownloaderManager(
    contextWrapper: ContextWrapper,
    val db: DownloadDatabaseClient,
    var config: DownloaderConfig = DownloaderConfig.Builder(contextWrapper).build(),
    val downloadDispatcher: DownloadDispatcherImpl = DownloadDispatcherImpl(
        contextWrapper,
        db,
        config = config
    ),
    val httpClientFactory: HttpClientFactory,
) : IDownloader by DownloadImpl(downloadDispatcher, db) {
    init {
        DownloadGlobal.setDownloaderManager(this)
    }
}
