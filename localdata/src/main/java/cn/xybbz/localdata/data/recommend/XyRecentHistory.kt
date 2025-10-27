package cn.xybbz.localdata.data.recommend

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xy_recent_recommend_history")
data class XyRecentHistory(
    /**
     * 歌曲id
     */
    @PrimaryKey
    val songId: String,
    /**
     * 加入缓存的时间，用于判断过期
     */
    val timestamp: Long = System.currentTimeMillis()
)
