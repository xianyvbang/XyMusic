package cn.xybbz.localdata.data.progress

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 记录专辑开启播放历史状态
 */
@Entity(tableName = "xy_enable_progress",
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
