package cn.xybbz.localdata.data.genre

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 流派
 */
@Entity(
    tableName = "xy_genre",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("connectionId")]
)
data class XyGenre(
    @PrimaryKey
    val itemId: String,
    /**
     * 图片地址
     */
    val pic: String,
    /**
     * 流派名称
     */
    val name: String,
    /**
     * 连接id
     */
    val connectionId: Long,
    /**
     * 创建时间
     */
    val createTime: Long = System.currentTimeMillis()
)
