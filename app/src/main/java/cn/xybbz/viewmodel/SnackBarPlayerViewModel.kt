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

package cn.xybbz.viewmodel

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.room.Transaction
import cn.xybbz.R
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.music.DownloadCacheController
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.download.core.DownloadRequest
import cn.xybbz.config.select.SelectControl
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.enums.DownloadTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(UnstableApi::class)
class SnackBarPlayerViewModel @Inject constructor(
    val musicController: MusicController,
    val db: DatabaseClient,
    private val downloadCacheController: DownloadCacheController,
    val dataSourceManager: DataSourceManager,
    val selectControl: SelectControl,
    private val downloadManager: DownLoadManager
) : ViewModel() {


    /**
     * 清空播放列表
     */
    @Transaction
    suspend fun clearPlayer() {
        db.playerDao.removeByDatasource()
        db.musicDao.removePlayQueueMusic()
        viewModelScope.launch {
            downloadCacheController.clearCache()
        }
    }

    fun downloadMusics() {
        viewModelScope.launch {
            val downloadTypes =
                dataSourceManager.dataSourceType?.getDownloadType() ?: DownloadTypes.APK
            val itemIds = selectControl.selectMusicIdList.toList()
            val musicList = db.musicDao.selectByIds(itemIds)
            val requests = musicList.map { musicData ->
                DownloadRequest(
                    url = musicData.downloadUrl,
                    fileName = musicData.name + "." + musicData.container,
                    fileSize = musicData.size ?: 0,
                    uid = musicData.itemId,
                    title = musicData.name,
                    type = downloadTypes,
                    cover = musicData.pic,
                    duration = musicData.runTimeTicks,
                    connectionId = dataSourceManager.getConnectionId(),
                    music = musicData
                )
            }
            if (requests.isNotEmpty()) {
                downloadManager.enqueue(
                    *requests.toTypedArray()
                )
                MessageUtils.sendPopTip(R.string.add_download_list)
            }

        }.invokeOnCompletion { selectControl.dismiss() }

    }
}