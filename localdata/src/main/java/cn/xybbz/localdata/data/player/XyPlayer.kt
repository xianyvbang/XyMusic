package cn.xybbz.localdata.data.player

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum

/**
 * 正在播放配置存储实体类
 * @author 刘梦龙
 * @date 2025/04/10
 * @constructor 创建[XyPlayer]
 * @param [id] ID
 * @param [connectionId] 连接ID
 * @param [dataType] 数据类型
 * @param [playerIndex] 指数
 * @param [headTime] 头部时间
 * @param [endTime] 结束时间
 * @param [playerType] 玩家类型
 * @param [pageNum] 页数
 * @param [ifSkip] 如果跳过
 * @param [albumId] 专辑ID
 * @param [artistId] 艺术家ID
 */
@Entity(
    tableName = "xy_player",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("connectionId")]
)
data class XyPlayer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /**
     * 数据源类型
     */
    val connectionId: Long?,
    /**
     * 连接id
     */
    val dataType: MusicPlayTypeEnum,
    /**
     * 最后播放音乐id
     */
    val musicId: String = "",
    /**
     * 片头跳过时间
     */
    val headTime: Long = 0,
    /**
     * 片尾跳过时间
     */
    val endTime: Long = 0,
    /**
     * 当前播放顺序类型
     */
    val playerType: PlayerTypeEnum = PlayerTypeEnum.SEQUENTIAL_PLAYBACK,
    /**
     * 页码
     */
    val pageNum: Int = 0,
    /**
     * 分页大小
     */
    val pageSize: Int,
    /**
     * 是否设置跳过头尾
     */
    val ifSkip: Boolean = false,
    /**
     * 专辑id
     */
    val albumId: String = "",
    /**
     * 艺术家id
     */
    val artistId: String? = "",
)