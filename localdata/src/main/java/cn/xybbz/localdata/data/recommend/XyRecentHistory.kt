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

package cn.xybbz.localdata.data.recommend

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.xybbz.localdata.data.connection.ConnectionConfig

@Entity(
    tableName = "xy_recent_recommend_history",
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [Index("connectionId")]
)
data class XyRecentHistory(
    /**
     * 歌曲id
     */
    @PrimaryKey(autoGenerate = false)
    val songId: String,

    /**
     * 链接id
     */
    val connectionId: Long,
    /**
     * 加入缓存的时间，用于判断过期
     */
    val timestamp: Long = System.currentTimeMillis()
)
