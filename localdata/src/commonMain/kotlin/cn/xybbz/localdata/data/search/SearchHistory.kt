package cn.xybbz.localdata.data.search

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock

/**
 * 搜索历史
 */
@Entity(
    tableName = "search_history",
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
    val createTime: Long = Clock.System.now().toEpochMilliseconds()
)
