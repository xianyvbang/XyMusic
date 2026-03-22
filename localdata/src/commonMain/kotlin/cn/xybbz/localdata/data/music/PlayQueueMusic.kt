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
import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 播放队列音乐
 * @author 刘梦龙
 * @date 2025/05/19
 * @constructor 创建[PlayQueueMusic]
 * @param [musicId] 音乐ID
 * @param [cachedAt] 缓存
 */
@Entity(
    primaryKeys = ["musicId", "connectionId"],
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("musicId"), Index("connectionId")]
)
data class PlayQueueMusic(
    val musicId: String,
    val connectionId: Long,
    val index: Int,
    val cachedAt: Long = System.currentTimeMillis()
)
