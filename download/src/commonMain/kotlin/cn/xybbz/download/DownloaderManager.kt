package cn.xybbz.download

import cn.xybbz.config.download.core.IDownloader
import cn.xybbz.download.core.DownloadDispatcherImpl
import cn.xybbz.download.core.DownloadImpl
import cn.xybbz.download.database.DownloadDatabaseClient

class DownloaderManager(
    private val downloadDispatcher: DownloadDispatcherImpl,
    private val db: DownloadDatabaseClient
) : IDownloader by DownloadImpl(downloadDispatcher, db) {

}
