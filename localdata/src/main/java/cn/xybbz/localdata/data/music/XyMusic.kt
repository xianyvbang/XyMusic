package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig
import com.squareup.moshi.JsonClass
import java.util.UUID

@Entity(
    tableName = "xy_music",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("connectionId")]
)
@JsonClass(generateAdapter = true)
data class XyMusic(
    /**
     * 数据音乐
     */
    @PrimaryKey(autoGenerate = false)
    val itemId: String = "",
    /**
     * 音乐图片
     */
    val pic: String? = "",
    /**
     * 音乐名称
     */
    val name: String = "",
    /**
     * 音乐地址
     */
    val musicUrl: String = "",

    /**
     * 下载地址
     */
    val downloadUrl: String,

    /**
     * 专辑id
     */
    val album: String = "",
    /**
     * 专辑名称
     */
    val albumName: String? = "",
    /**
     * 流派id
     */
    val genreIds: String?,
    /**
     * 连接地址
     */
    val connectionId: Long,
    /**
     * 数据类型
     */
//    val dataType: MusicPlayTypeEnum,
    /**
     * 音乐艺术家名称
     */
    val artists: String? = "",

    /**
     * 音乐艺术家id->使用"/"分割
     */
    val artistIds: String?,
    /**
     * 专辑艺术家
     */
    val albumArtist: String? = "",
    /**
     * 专辑音乐艺术家id->使用"/"分割
     */
    val albumArtistIds: String? = "",
    /**
     * 年份
     */
    val year: Int? = null,

    /**
     * 播放次数
     */
    val playedCount: Int,

    /**
     * 是否已经收藏
     */
   val ifFavoriteStatus: Boolean,

    /**
     * 是否有歌词
     */
    val ifLyric: Boolean,

    /**
     * 歌词信息
     */
    val lyric: String?,

    /**
     * 实际路径
     */
    val path: String,
    /**
     * 比特率
     */
    val bitRate: Int? = 0,
    /**
     * 采样率
     */
    val sampleRate: Int? = 0,
    /**
     * 位深
     */
    val bitDepth: Int? = 0,
    /**
     * 大小
     */
    val size: Long? = 0,
    /**
     * 时长=
     * 1383720630 / 10000/1000/60
     */
    val runTimeTicks: Long = 0,
    /**
     * 格式
     */
    val container: String? = "",
    /**
     * 编码
     */
    val codec: String? = "",
    /**
     * 播放列表Id
     */
    val playlistItemId: String?,
    /**
     * 最近播放时间
     */
    val lastPlayedDate: Long,
    /**
     * 创建时间
     */
    val createTime: Long = System.currentTimeMillis()
) {
    /**
     * 播放会话id,
     */
    @Ignore
    val playSessionId: String = UUID.randomUUID().toString()
}