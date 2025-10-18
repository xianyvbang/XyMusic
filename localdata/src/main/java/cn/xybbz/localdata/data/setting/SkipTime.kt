package cn.xybbz.localdata.data.setting

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig

@Entity(
    tableName = "skip_time",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
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