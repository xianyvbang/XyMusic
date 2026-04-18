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
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * 侧边栏歌单 ViewModel。
 */
@KoinViewModel
class SidebarPlaylistViewModel(
    private val db: LocalDatabaseClient,
    private val dataSourceManager: DataSourceManager
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<XyAlbum>>(emptyList())
    val playlists: StateFlow<List<XyAlbum>> get() = _playlists

    private var playlistJob: Job? = null

    init {
        startPlaylistObserver()
        observePlaylistChanges()
        refreshPlaylists()
    }

    private fun observePlaylistChanges() {
        viewModelScope.launch {
            dataSourceManager.mergeFlow.collect {
                refreshPlaylists()
            }
        }
    }

    private fun startPlaylistObserver() {
        playlistJob?.cancel()
        playlistJob = viewModelScope.launch {
            db.albumDao
                .selectPlaylistFlow()
                .distinctUntilChanged()
                .collect { list ->
                    _playlists.value = list
                }
        }
    }

    fun refreshPlaylists() {
        viewModelScope.launch {
            dataSourceManager.getPlaylists()
        }
    }

    fun savePlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dataSourceManager.addPlaylist(name)
        }
    }
}
