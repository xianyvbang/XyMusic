package cn.xybbz.download.core

import cn.xybbz.platform.ContextWrapper

class DownloaderConfig(
    val maxConcurrentDownloads: Int,
    val finalDirectory: String,
) {

    // Builder 保留在 commonMain，但默认目录解析走平台 actual。
    class Builder(contextWrapper: ContextWrapper? = null) {
        private var maxConcurrentDownloads: Int = 3
        private var finalDirectory: String =
            DownloadPlatformFiles.defaultDownloadDirectory(contextWrapper)

        fun setMaxConcurrentDownloads(count: Int) = apply {
            maxConcurrentDownloads = count
        }

        fun setFinalDirectory(dirPath: String) = apply {
            finalDirectory = dirPath
        }

        fun build(): DownloaderConfig {
            return DownloaderConfig(
                maxConcurrentDownloads = maxConcurrentDownloads,
                finalDirectory = finalDirectory,
            )
        }
    }
}
