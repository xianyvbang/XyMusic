package cn.xybbz.localdata.data.remote

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig
import java.time.Instant

/**
 * 记录分页信息
 * @author Administrator
 * @date 2025/02/21
 * @constructor 创建[RemoteCurrent]
 * @param [id]
 * @param [nextKey]
 * @param [prevKey]
 * @param [total]
 * @param [connectionId]
 * @param [createTime]
 */
@Entity(
    tableName = "remote_current",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("connectionId")]
)
data class RemoteCurrent(
    @PrimaryKey
    val id: String = "",
    val nextKey: Int = 0,
    val prevKey: Int = 1,
    val total: Int = 0,
    //是否刷新数据
    val refresh: Boolean,
    /**
     * 连接id
     */
    val connectionId: Long,
    val createTime: Long = Instant.now().toEpochMilli()
)