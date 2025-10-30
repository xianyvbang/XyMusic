package cn.xybbz.api.client.navidrome.data

import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class SongItem(
    val id: String,

    /**
     * 数据id
     */
    val mediaFileId:String? = null,
    /**
     * 歌曲标题
     */
    val title: String,
    /**
     * 艺术家编码
     */
    val artistId: String,
    /**
     * 艺术家名称
     */
    val artist: String,
    /**
     * 专辑艺术家编码
     */
    val albumArtistId: String,
    /**
     * 专辑艺术家名称
     */
    val albumArtist: String,
    /**
     * 专辑编码
     */
    val albumId: String,
    /**
     * 专辑名称
     */
    val album: String,
    /**
     * 路径地址
     */
    val path: String,
    /**
     * 媒体库编码
     */
    val libraryId: Long,
    /**
     * 媒体库路径
     */
    val libraryPath: String,
    /**
     * 播放次数
     */
    val playCount: Long? = null,
    /**
     * 播放时间
     */
    val playDate: LocalDateTime? = null,
    /**
     * 是否收藏
     */
    val starred: Boolean? = null,
    /**
     * 收藏时间
     */
    val starredAt: LocalDateTime? = null,
    /**
     * 文件夹编码
     */
    val folderId: String,
    /**
     * 是否有封面图片
     */
    val hasCoverArt: Boolean,
    /**
     * 音轨编号
     */
    val trackNumber: Long,
    /**
     * 音乐碟号
     */
    val discNumber: Long,
    /**
     * 发行年份
     */
    val year: Int,
    /**
     * 发行时间
     */
    val date: String? = null,
    /**
     * 最初发行年份
     */
    val originalYear: Long,
    /**
     * 发行年份
     */
    val releaseYear: Long,
    /**
     * 文件大小/字节
     */
    val size: Long,
    /**
     * 文件扩展名
     */
    val suffix: String,
    /**
     * 时长/秒
     */
    val duration: Double,
    /**
     * 比特率
     */
    val bitRate: Int,
    /**
     * 采样率
     */
    val sampleRate: Int,
    /**
     * 位深
     */
    val bitDepth: Int,
    /**
     * 通道号
     */
    val channels: Long,
    /**
     * 流派名称
     */
    val genre: String,
    /**
     * 流派信息
     */
    val genres: List<Genre>? = null,
    /**
     * 排序标题
     */
    val orderTitle: String,
    /**
     * 排序专辑名称
     */
    val orderAlbumName: String,
    /**
     * 排序艺术家名称
     */
    val orderArtistName: String,
    /**
     * 排序专辑艺术家名称
     */
    val orderAlbumArtistName: String,
    /**
     * todo 不知道是用来做什么的,是否编辑?
     */
    val compilation: Boolean,
    /**
     * 描述
     */
    val comment: String? = null,
    /**
     * 歌词
     */
    val lyrics: String? = "",
    val explicitStatus: String,
    /**
     * 专辑增益
     */
    val rgAlbumGain: Double,
    /**
     * 专辑峰值
     */
    val rgAlbumPeak: Double,
    /**
     * 通道增益
     */
    val rgTrackGain: Double,
    /**
     * 通道峰值
     */
    val rgTrackPeak: Double,
    val missing: Boolean,
    /**
     * 诞生时间
     */
    val birthTime: LocalDateTime,
    /**
     * 创建时间
     */
    val createdAt: LocalDateTime,
    /**
     * 更新时间
     */
    val updatedAt: LocalDateTime,
    /**
     * 星级
     */
    val rating: Int? = null
)
