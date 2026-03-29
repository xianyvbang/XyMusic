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
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlin.time.Clock

@Entity(
    tableName = "xy_daily_recommend_history",
    primaryKeys = ["songId", "connectionId"],
    foreignKeys = [ForeignKey(
        entity = ConnectionConfig::class,
        parentColumns = ["id"],
        childColumns = ["connectionId"],
        onDelete = CASCADE
    )],
    indices = [
        Index("connectionId"),
        Index(value = ["connectionId", "timestamp"]),
        Index(value = ["connectionId", "mediaLibraryId", "timestamp"])
    ]
)
data class XyDailyRecommendHistory(
    /**
     * Song id
     */
    val songId: String,

    /**
     * Connection id
     */
    val connectionId: Long,

    /**
     * 当前推荐生成时绑定的媒体库标识（来自 ConnectionConfig.libraryIds）
     */
    val mediaLibraryId: String? = null,

    /**
     * Recommend order in one generation batch
     */
    val recommendIndex: Int = 0,

    /**
     * Cache timestamp for expire and ordering
     */
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)
