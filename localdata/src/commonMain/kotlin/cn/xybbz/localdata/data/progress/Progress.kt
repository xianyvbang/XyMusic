package cn.xybbz.localdata.data.progress

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig
import java.time.Instant

/**
 * 播放历史进度,只存储有声小说类型的,每个有声小说只有一条,只存储最新的那一条
 */
@Entity(
    "progress",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("connectionId")]
)
data class Progress(
    @PrimaryKey
    val musicId: String = "",
    val musicName: String,
    val albumId: String = "",
    val progress: Long = 0,
    /**
     * 进度百分比
     */
    val progressPercentage: Int = 0,
    /**
     * 有声小说位置索引
     */
    val index: Int = 0,
    /**
     * 连接id
     */
    val connectionId: Long,
    val createTime: Long = Instant.now().toEpochMilli()
)
