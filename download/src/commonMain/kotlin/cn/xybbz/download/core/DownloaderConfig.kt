package cn.xybbz.download.core

import cn.xybbz.platform.ContextWrapper

class DownloaderConfig(
    val maxConcurrentDownloads: Int,
    val finalDirectory: String,
    /**
     * 平台默认下载目录，用于设置页展示与「恢复默认目录」回落到正确路径。
     */
    val defaultFinalDirectory: String = finalDirectory,
) {

    /**
     * 计算当前实际生效的下载目录。
     *
     * 自定义目录可能因为路径被删除、无写权限或磁盘空间不足而不可用，
     * 这类情况统一回落到平台默认目录，避免阻塞下载功能。
     */
    fun usableFinalDirectory(): String =
        DownloadDirectorySupport.resolve(finalDirectory, defaultFinalDirectory)

    // Builder 保留在 commonMain，但默认目录解析走平台 actual。
    class Builder(contextWrapper: ContextWrapper) {
        private var maxConcurrentDownloads: Int = 3

        // 平台默认下载目录在构造时解析一次，后续不再变化。
        private val defaultFinalDirectory: String =
            DownloadPlatformFiles.defaultDownloadDirectory(contextWrapper)

        private var finalDirectory: String = defaultFinalDirectory

        fun setMaxConcurrentDownloads(count: Int) = apply {
            maxConcurrentDownloads = count
        }

        // 传入空字符串表示沿用平台默认目录，避免把空路径写入配置。
        fun setFinalDirectory(dirPath: String) = apply {
            finalDirectory = dirPath.trim().ifEmpty { defaultFinalDirectory }
        }

        fun build(): DownloaderConfig {
            return DownloaderConfig(
                maxConcurrentDownloads = maxConcurrentDownloads,
                finalDirectory = finalDirectory,
                defaultFinalDirectory = defaultFinalDirectory,
            )
        }
    }
}
