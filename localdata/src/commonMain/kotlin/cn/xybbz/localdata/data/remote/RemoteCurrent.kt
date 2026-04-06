package cn.xybbz.localdata.data.remote

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock

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
    val createTime: Long = Clock.System.now().toEpochMilliseconds()
)