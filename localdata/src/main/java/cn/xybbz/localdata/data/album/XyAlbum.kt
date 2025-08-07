package cn.xybbz.localdata.data.album

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig
import java.time.Instant

@Entity(
    tableName = "xy_album",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("connectionId")]
)
data class XyAlbum(
    /**
     * 数据id
     */
    @PrimaryKey(autoGenerate = false)
    val itemId: String = "",
    /**
     * 音乐图片
     */
    val pic: String? = "",
    /**
     * 专辑名称
     */
    val name: String = "",

    /**
     * 艺术家
     */
    val artists: String? = "",
    /**
     * 音乐艺术家id->使用"/"分割
     */
    val artistIds: String? = "",
    /**
     * 类型id
     */
    val genreIds: String? = "",
    /**
     * 连接id
     */
    val connectionId: Long,
    /**
     * 年份
     */
    val year: Int? = null,
    /**
     * 发行时间
     */
    val premiereDate: Long? = null,
    /**
     * 是否已经收藏
     */
    val ifFavorite: Boolean = false,
    /**
     * 数据类型
     */
//    val dataType: MusicPlayTypeEnum,
    /**
     * 是否歌单
     */
    val ifPlaylist: Boolean,
    /**
     * 歌曲数量
     */
    val musicCount: Long,
    /**
     * 创建时间
     */
    val createTime: Long = Instant.now().toEpochMilli(),
)
