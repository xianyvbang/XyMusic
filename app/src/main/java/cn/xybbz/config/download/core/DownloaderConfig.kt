package cn.xybbz.config.download.core

import android.content.Context
import android.os.Environment
import java.io.File

class DownloaderConfig(
    val maxConcurrentDownloads: Int,
    val finalDirectory: String
) {

    class Builder(context: Context) {
        private var maxConcurrentDownloads: Int = 3
        private var finalDirectory: String =
            (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: File(context.filesDir, "downloads")).absolutePath
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