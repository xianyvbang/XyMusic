package cn.xybbz.localdata.data.library

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 媒体库实体类
 */
@Entity(
    tableName = "xy_library",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("connectionId")]
)
data class XyLibrary(
    @PrimaryKey
    val id: String,
    val collectionType: String,
    /**
     * 连接id
     */
    val connectionId: Long,
    val name: String
)
