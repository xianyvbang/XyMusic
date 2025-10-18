package cn.xybbz.localdata.data.count

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 主要数据数量
 */
@Entity(
    tableName = "xy_data_count",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("connectionId")]
)
data class XyDataCount(
    /**
     * 连接id
     */
    @PrimaryKey(autoGenerate = false)
    val connectionId: Long,
    /**
     * 音乐数量
     */
    val musicCount: Int = 0,
    /**
     * 专辑数量
     */
    val albumCount: Int = 0,
    /**
     * 艺术家数量
     */
    val artistCount: Int = 0,
    /**
     * 歌单数量
     */
    val playlistCount: Int = 0,
    /**
     * 流派数量
     */
    val genreCount: Int = 0,
    /**
     * 收藏数量
     */
    val favoriteCount: Int = 0,
    /**
     * 更新时间
     */
    val createTime: Long = System.currentTimeMillis()

)
