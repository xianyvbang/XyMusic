package cn.xybbz.download

object DownloadGlobal {
    lateinit var downloaderManagerGlobal: DownloaderManager
        private set

    fun setDownloaderManager(downloaderManager: DownloaderManager) {
        downloaderManagerGlobal = downloaderManager
    }
}