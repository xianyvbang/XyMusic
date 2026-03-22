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

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 底部弹窗歌单ViewModel
 */
@HiltViewModel
class PlaylistBottomViewModel @Inject constructor(
    private val db: DatabaseClient,
    val dataSourceManager: DataSourceManager

    ) : ViewModel() {

    /**
     * 歌单列表
     */
    var playlists by mutableStateOf(emptyList<XyAlbum>())
        private set

    var isInit by mutableStateOf(false)
        private set

    private var playlistJob: Job? = null


    init {
        observeLoginSuccessForPlaylist()
        isInit = true
    }

    private fun observeLoginSuccessForPlaylist() {
        viewModelScope.launch {
            dataSourceManager.mergeFlow.collect {
                startPlaylistObserver()
            }
        }
    }
    /**
     * 获得歌单数据
     */
    private fun startPlaylistObserver() {
        // 取消之前的 Job，避免重复订阅
        playlistJob?.cancel()

        playlistJob = viewModelScope.launch {
            db.albumDao
                .selectPlaylistFlow()
                .distinctUntilChanged()
                .collect { list ->
                    if (list.isNotEmpty()) {
                        playlists = list
                    }
                }
        }
    }


    suspend fun getServerPlaylists() {
        Log.i("=====","获得歌单数据")
        dataSourceManager.getPlaylists()
    }
}