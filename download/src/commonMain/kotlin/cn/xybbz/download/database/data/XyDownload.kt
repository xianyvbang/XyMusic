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

package cn.xybbz.download.database.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.enums.DownloadTypes
import kotlin.time.Clock

@Entity(tableName = "xy_download")
data class XyDownload(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val tempFilePath: String,

    val typeData: DownloadTypes = DownloadTypes.APK,
    var progress: Float = 0f,
    var totalBytes: Long = 0L,
    var downloadedBytes: Long = 0L,

    var status: DownloadStatus = DownloadStatus.QUEUED,

    var error:String? = null,
    val uid: String? = null,
    val title: String? = null,
    val cover: String? = null,
    val duration: Long? = null,
    //资源id
    val mediaLibraryId: String? = null,
    val libraryId: String? = null,
    val extend: String? = null,
    val data: String? = null,

    val updateTime: Long = Clock.System.now().toEpochMilliseconds(),
    val createTime: Long = Clock.System.now().toEpochMilliseconds(),
)
