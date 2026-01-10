/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.localdata.data.music

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import cn.xybbz.localdata.converter.StringListTypeConverter
import cn.xybbz.localdata.data.connection.ConnectionConfig

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
@TypeConverters(StringListTypeConverter::class)
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
    val genreIds: List<String>? = null,
    /**
     * 连接地址
     */
    val connectionId: Long,
    /**
     * 音乐艺术家名称
     */
    val artists: List<String>? = emptyList(),

    /**
     * 音乐艺术家id
     */
    val artistIds: List<String>? = null,
    /**
     * 专辑艺术家
     */
    val albumArtist: List<String>? = null,
    /**
     * 专辑音乐艺术家id
     */
    val albumArtistIds: List<String>? = null,
    /**
     * 年份
     */
    val year: Int? = null,

    /**
     * 播放次数
     */
    val playedCount: Int = 0,

    /**
     * 是否已经收藏
     */
    val ifFavoriteStatus: Boolean = false,

    /**
     * 是否有歌词
     */
    val ifLyric: Boolean = false,

    /**
     * 歌词信息
     */
    val lyric: String? = "",

    /**
     * 实际路径
     */
    val path: String = "",
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
    val playlistItemId: String? = null,
    /**
     * plex播放key
     */
    val plexPlayKey: String? = null,
    /**
     * 最近播放时间
     */
    val lastPlayedDate: Long = System.currentTimeMillis(),
    /**
     * 创建时间
     */
    val createTime: Long = System.currentTimeMillis()
) {
    fun toPlayMusic(): XyPlayMusic {
        return XyPlayMusic(
            itemId = itemId,
            pic = pic,
            name = name,
            album = album,
            container = container,
            artists = artists,
            size = size,
            filePath = null,
            runTimeTicks = runTimeTicks,
            plexPlayKey = plexPlayKey,
            artistIds = artistIds
        )
    }
}