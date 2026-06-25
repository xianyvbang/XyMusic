package cn.xybbz.download.core


data class DownloadRequest(
    val url: String,
    val filePath: String? = null,
    val fileName: String,
    val fileSize: Long,
    // 可选的文件 MD5，用于下载完成后做内容完整性校验。
    val expectedMd5: String? = null,
    // --- 选填元数据 ---
    val uid: String? = null,     // 用于业务方追踪任务
    val title: String? = null,
    val type: String,
    val cover: String? = null,
    val duration: Long = 0L,
    val mediaLibraryId: String? = null,
    val extend: String? = null,
    val data: String? = null,
)
