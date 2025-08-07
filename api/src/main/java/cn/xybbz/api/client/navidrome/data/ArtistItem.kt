package cn.xybbz.api.client.navidrome.data

import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class ArtistItem(
    val id: String,
    val name: String,
    /**
     * 平分
     */
    val rating: Int? = null,
    /**
     * 是否收藏
     */
    val starred: Boolean? = null,
    /**
     * 收藏时间
     */
    val starredAt: LocalDateTime? = null,
    /**
     * 所属歌曲文件大小,单位字节
     */
    val size: Long,
    /**
     * 专辑数量
     */
    val albumCount: Long? = null,
    /**
     * 音乐数量
     */
    val songCount: Long? = null,
    /**
     * 播放次数
     */
    val playCount: Long? = null,
    /**
     * 播放时间
     */
    val playDate: LocalDateTime? = null,
    /**
     * 小图片链接地址
     */
    val smallImageUrl: String? = null,
    /**
     * 中等图像链接地址
     */
    val mediumImageUrl: String? = null,
    /**
     * 大图片链接
     */
    val largeImageUrl: String? = null,
    /**
     * 外部信息更新时间
     */
    val externalInfoUpdatedAt: LocalDateTime,
    /**
     * 是否数据缺失
     */
    val missing: Boolean,
    /**
     * 创建时间
     */
    val createdAt: LocalDateTime,
    /**
     * 更新时间
     */
    val updatedAt: LocalDateTime,
    /**
     * 按艺术家姓名排序
     */
    val sortArtistName: String? = null,
    /**
     * 艺术家排序名称
     */
    val orderArtistName: String,
    /**
     * mbz艺术家id
     */
    val mbzArtistId: String? = null
)
