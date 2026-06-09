package cn.xybbz.localdata.data.setting

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "skip_time",
    indices = [Index("connectionId")]
)
data class SkipTime(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /**
     * 专辑id
     */
    var albumId: String = "",
    /**
     * 头部跳过时间
     */
    var headTime: Long = 0,
    /**
     * 跳过片尾时间
     */
    var endTime: Long = 0,
    /**
     * 连接id
     */
    var connectionId: Long
)