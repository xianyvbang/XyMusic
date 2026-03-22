package cn.xybbz.config.download.core

import cn.xybbz.di.ContextWrapper

internal const val DOWNLOAD_FILE_NAME = "downloads"

class DownloaderConfig(
    val maxConcurrentDownloads: Int,
    val finalDirectory: String
) {

    class Builder(
        contextWrapper: ContextWrapper
    ) {
        private var maxConcurrentDownloads: Int = 3
        private var finalDirectory: String = getDefaultDownloadDirectory(contextWrapper)
        fun setMaxConcurrentDownloads(count: Int) = apply { this.maxConcurrentDownloads = count }
        fun setFinalDirectory(dirPath: String) =
            apply { this.finalDirectory = dirPath } // [CHANGE] File -> String

        fun build(): DownloaderConfig {
            return DownloaderConfig(
                maxConcurrentDownloads = maxConcurrentDownloads,
                finalDirectory = finalDirectory
            )
        }
    }
}

expect fun getDefaultDownloadDirectory(contextWrapper: ContextWrapper): String