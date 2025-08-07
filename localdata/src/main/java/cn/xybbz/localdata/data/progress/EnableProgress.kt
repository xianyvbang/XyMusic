package cn.xybbz.localdata.data.progress

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 记录专辑开启播放历史状态
 */
@Entity(tableName = "xy_enable_progress",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("connectionId")])
data class EnableProgress(
    /**
     * 专辑id
     */
    @PrimaryKey
    val albumId: String = "",
    /**
     * 是否开启播放历史
     */
    val ifEnableAlbumHistory: Boolean = false,
    /**
     * 连接id
     */
    val connectionId: Long
)
