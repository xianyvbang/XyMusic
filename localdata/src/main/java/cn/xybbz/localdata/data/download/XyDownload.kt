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

package cn.xybbz.localdata.data.download

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import cn.xybbz.localdata.converter.XyMusicTypeConverter
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.DownloadStatus
import cn.xybbz.localdata.enums.DownloadTypes

@Entity(tableName = "xy_download")
@TypeConverters(XyMusicTypeConverter::class)
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
    val connectionId: Long? = null,
    val extend:String? = null,
    val music: XyMusic? = null,

    val updateTime: Long = System.currentTimeMillis(),
    val createTime: Long = System.currentTimeMillis(),
){

    fun toPlayMusic(): XyPlayMusic?{
        return music?.let {
            XyPlayMusic(
                itemId = music.itemId,
                pic = music.pic,
                name = music.name,
                album = music.album,
                container = music.container,
                artists = music.artists,
                size = music.size,
                filePath = filePath,
                runTimeTicks = music.runTimeTicks,
                plexPlayKey = music.plexPlayKey,
                artistIds = music.artistIds
            )
        }
    }
}
