package cn.xybbz.api.client.navidrome.data

import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class PlaylistItemData(
    val id: String,
    /**
     * 名称
     */
    val name: String,
    /**
     * 描述
     */
    val comment: String,
    /**
     * 时长,单位秒
     */
    val duration: Double,
    /**
     * 大小 字节
     */
    val size: Long,
    /**
     * 歌曲数量
     */
    val songCount: Long,
    /**
     * 所属人员
     */
    val ownerName: String,
    /**
     * 所属人员id
     */
    val ownerId: String,
    /**
     * 是否公开
     */
    val public: Boolean,
    /**
     * 所属路径
     */
    val path: String,
    /**
     * 是否为自动导入
     */
    val sync: Boolean,
    /**
     * 创建时间
     */
    val createdAt: LocalDateTime,
    /**
     * 更新时间
     */
    val updatedAt: LocalDateTime
)
