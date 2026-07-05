package cn.xybbz.localdata.data.progress

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.music.XyMusic
import kotlin.time.Clock

/**
 * 播放历史进度,只存储有声小说类型的,每个有声小说只有一条,只存储最新的那一条
 */
@Entity(
    "progress",
    foreignKeys = [
        ForeignKey(
            entity = ConnectionConfig::class,
            parentColumns = ["id"],
            childColumns = ["connectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = XyMusic::class,
            parentColumns = ["itemId", "connectionId"],
            childColumns = ["musicId", "connectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("connectionId"), Index(value = ["musicId", "connectionId"])]
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
    val createTime: Long = Clock.System.now().toEpochMilliseconds()
)
