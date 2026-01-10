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

import androidx.room.Ignore
import androidx.room.TypeConverters
import cn.xybbz.localdata.converter.StringListTypeConverter
import java.util.UUID

/**
 * 音乐播放类
 */
@TypeConverters(StringListTypeConverter::class)
data class XyPlayMusic(
    val itemId: String,
    /**
     * 音乐图片
     */
    val pic: String?,
    /**
     * 音乐名称
     */
    val name: String,

    /**
     * 专辑id
     */
    val album: String,

    /**
     * 播放会话id,
     */
    val playSessionId: String = UUID.randomUUID().toString(),
    /**
     * 格式
     */
    val container: String?,
    /**
     * 音乐艺术家名称
     */
    val artists: List<String>?,
    /**
     * 艺术家id
     */
    val artistIds: List<String>?,

    /**
     * 是否已经收藏
     */
    val ifFavoriteStatus: Boolean = false,
    /**
     * 大小
     */
    val size: Long?,
    /**
     * 文件地址
     */
    val filePath: String?,
    /**
     * 时长 秒
     */
    val runTimeTicks: Long,
    /**
     * plex播放key
     */
    val plexPlayKey: String?
) {
    /**
     * 音乐地址
     */
    @Ignore
    private var musicUrl: String = ""

    fun getMusicUrl(): String {
        return musicUrl
    }

    fun setMusicUrl(musicUrl: String) {
        this.musicUrl = musicUrl
    }
}