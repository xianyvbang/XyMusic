package cn.xybbz.localdata.data.count

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock

/**
 * 主要数据数量
 */
@Entity(
    tableName = "xy_data_count",
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
    val musicCount: Int? = null,
    /**
     * 专辑数量
     */
    val albumCount: Int? = null,
    /**
     * 艺术家数量
     */
    val artistCount: Int? = null,
    /**
     * 歌单数量
     */
    val playlistCount: Int? = null,
    /**
     * 流派数量
     */
    val genreCount: Int? = null,
    /**
     * 收藏数量
     */
    val favoriteCount: Int? = null,
    /**
     * 创建时间
     */
    val createTime: Long = Clock.System.now().toEpochMilliseconds()

)
