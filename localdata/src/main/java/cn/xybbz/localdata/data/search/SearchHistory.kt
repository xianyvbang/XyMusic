package cn.xybbz.localdata.data.search

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig
import java.time.Instant

/**
 * 搜索历史
 */
@Entity(
    tableName = "search_history",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("connectionId")]
)
data class SearchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /**
     * 搜索内容
     */
    val searchQuery: String = "",

    /**
     * 链接id
     */
    val connectionId: Long,
    /**
     * 搜索时间
     */
    val createTime: Long = Instant.now().toEpochMilli()
)