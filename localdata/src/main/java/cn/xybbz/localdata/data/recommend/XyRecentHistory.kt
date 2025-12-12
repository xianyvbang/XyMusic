package cn.xybbz.localdata.data.recommend

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig

@Entity(
    tableName = "xy_recent_recommend_history",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )]
)
data class XyRecentHistory(
    /**
     * 歌曲id
     */
    @PrimaryKey(autoGenerate = false)
    val songId: String,

    /**
     * 链接id
     */
    val connectionId: Long,
    /**
     * 加入缓存的时间，用于判断过期
     */
    val timestamp: Long = System.currentTimeMillis()
)
