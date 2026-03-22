package cn.xybbz.localdata.data.album

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 首页专辑
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[HomeAlbum]
 * @param [albumId] 专辑ID
 * @param [cachedAt] 缓存时间
 */
@Entity(
    primaryKeys = ["albumId", "connectionId"],
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("albumId"), Index("connectionId")]
)
data class HomeAlbum(
    val albumId: String,
    val connectionId: Long,
    val index:Int,
    val cachedAt: Long = System.currentTimeMillis()// 缓存时间戳（System.currentTimeMillis()）
)
