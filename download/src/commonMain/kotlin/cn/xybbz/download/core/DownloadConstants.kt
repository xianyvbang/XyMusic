package cn.xybbz.download.core

internal object DownloadConstants {
    const val WORK_INPUT_DOWNLOAD_ID = "download_id"
    const val LOG_TAG = "Download"

    // 下载临时文件目录名称，所有平台保持一致便于排查和清理。
    const val TEMP_DOWNLOAD_DIRECTORY_NAME = "xy-downloads"
}
