package cn.xybbz.config.download.core

import cn.xybbz.localdata.enums.DownloadTypes

data class DownloadRequest (
    val url: String,
    val filePath: String? = null,
    val fileName: String,
    val fileSize: Long,
    // --- 选填元数据 ---
    val uid: String? = null,     // 用于业务方追踪任务
    val title:String? = null,
    val type: DownloadTypes = DownloadTypes.APK,
    val cover: String? = null,
    val duration: Long = 0L,
    val connectionId: Long? = null,
    )