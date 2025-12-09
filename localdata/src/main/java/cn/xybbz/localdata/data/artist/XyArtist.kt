package cn.xybbz.localdata.data.artist

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 艺术家项目
 * @author 刘梦龙
 * @date 2024/12/05
 * @constructor 创建[XyArtist]
 * @param [artistId] 艺术家编号
 * @param [pic] 图片
 * @param [backdropImage] 背景图片
 * @param [name] 姓名
 * @param [sortName] 排序名称-汉语拼音,其他语言暂时未知
 * @param [dataSource] 数据来源
 */
@Entity(
    tableName = "xy_artist",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = ForeignKey.Companion.CASCADE
    )],
    indices = [Index("connectionId")]
)
data class XyArtist(
    @PrimaryKey
    val artistId: String = "",
    val pic: String? = "",
    val backdrop:String?,
    val describe: String? = "",
    val name: String? = null,
    val sortName: String? = "",
    /**
     * 连接地址
     */
    val connectionId: Long,
    val musicCount: Int? = 0,
    val albumCount: Int? = 0,
    val selectChat: String,
    /**
     * 加载数据所在索引
     */
    val indexNumber: Int = 0,
    /**
     * 是否已经收藏
     */
    @Ignore val ifFavorite: Boolean,
) {
    constructor(
        artistId: String = "",
        pic: String? = "",
        backdrop: String?,
        describe: String? = "",
        name: String? = null,
        sortName: String? = "",
        connectionId: Long,
        musicCount: Int? = 0,
        albumCount: Int? = 0,
        selectChat: String,
        indexNumber: Int = 0
    ) : this(
        artistId = artistId,
        pic = pic,
        backdrop = backdrop,
        describe = describe,
        name = name,
        sortName = sortName,
        connectionId = connectionId,
        musicCount = musicCount,
        albumCount = albumCount,
        selectChat = selectChat,
        indexNumber = indexNumber,
        ifFavorite = false
    )
}