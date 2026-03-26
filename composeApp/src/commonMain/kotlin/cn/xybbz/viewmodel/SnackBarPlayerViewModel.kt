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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.music.DownloadCacheCommonController
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.select.SelectControl
import cn.xybbz.localdata.config.DatabaseClient
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SnackBarPlayerViewModel (
    val musicController: MusicCommonController,
    val db: DatabaseClient,
    private val downloadCacheController: DownloadCacheCommonController,
    val dataSourceManager: DataSourceManager,
    val selectControl: SelectControl,
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

        }.invokeOnCompletion { selectControl.dismiss() }

    }
}
