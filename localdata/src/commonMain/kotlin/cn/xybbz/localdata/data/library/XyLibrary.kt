package cn.xybbz.localdata.data.library

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 媒体库实体类
 */
@Entity(
    tableName = "xy_library",
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
