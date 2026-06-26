package cn.xybbz.localdata.data.album

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import cn.xybbz.database.converter.JsonStringListTypeConverter
import kotlin.time.Clock

@Entity(
    tableName = "xy_album",
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
     * 艺术家名称列表
     */
    @field:TypeConverters(JsonStringListTypeConverter::class)
    val artists: List<String>? = emptyList(),
    /**
     * 音乐艺术家 id 列表
     */
    @field:TypeConverters(JsonStringListTypeConverter::class)
    val artistIds: List<String>? = emptyList(),
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
    @Ignore val ifFavorite: Boolean,
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
    val createTime: Long = Clock.System.now().toEpochMilliseconds(),
) {
    constructor(
        itemId: String = "",
        pic: String? = "",
        name: String = "",
        artists: List<String>? = emptyList(),
        artistIds: List<String>? = emptyList(),
        genreIds: String? = "",
        connectionId: Long,
        year: Int? = null,
        premiereDate: Long? = null,
        ifPlaylist: Boolean,
        musicCount: Long,
        createTime: Long = Clock.System.now().toEpochMilliseconds()
    ) : this(
        itemId = itemId,
        pic = pic,
        name = name,
        artists = artists,
        artistIds = artistIds,
        genreIds = genreIds,
        connectionId = connectionId,
        year = year,
        premiereDate = premiereDate,
        ifPlaylist = ifPlaylist,
        musicCount = musicCount,
        createTime = createTime,
        ifFavorite = false
    )
}
