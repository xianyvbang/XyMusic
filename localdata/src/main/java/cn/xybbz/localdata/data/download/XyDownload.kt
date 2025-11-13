package cn.xybbz.localdata.data.download

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.xybbz.localdata.enums.DownloadStatus
import cn.xybbz.localdata.enums.DownloadTypes

@Entity(tableName = "xy_download")
data class XyDownload(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val tempFilePath: String,

    val typeData: DownloadTypes = DownloadTypes.APK,
    var progress: Float = 0f,
    var totalBytes: Long = 0L,
    var downloadedBytes: Long = 0L,

    var status: DownloadStatus = DownloadStatus.QUEUED,

    var error:String? = null,

    val uid: String? = null,
    val title: String? = null,
    val cover: String? = null,
    val duration: Long? = null,
    val connectionId: Long? = null,

    val updateTime: Long = System.currentTimeMillis(),
    val createTime: Long = System.currentTimeMillis(),
)
