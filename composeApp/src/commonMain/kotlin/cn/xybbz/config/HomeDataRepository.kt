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

package cn.xybbz.config

import cn.xybbz.api.TokenServer
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.utils.Log
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.count.XyDataCount
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class HomeDataRepository(
    private val db: LocalDatabaseClient
) {

    private val _mostPlayedMusic = MutableStateFlow<List<XyMusic>>(emptyList())
    private val _newestAlbums = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _recentMusic = MutableStateFlow<List<XyMusic>>(emptyList())
    private val _recentAlbums = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _mostPlayedAlbums = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _recommendedMusic = MutableStateFlow<List<XyMusic>>(emptyList())
    private val _playlists = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _dataCount = MutableStateFlow<XyDataCount?>(null)

    //最多播放音乐
    val mostPlayedMusic: StateFlow<List<XyMusic>> get() = _mostPlayedMusic
    //最新专辑
    val newestAlbums: StateFlow<List<XyAlbum>> get() = _newestAlbums
    //最近播放音乐
    val recentMusic: StateFlow<List<XyMusic>> get() = _recentMusic
    //最近播放专辑
    val recentAlbums: StateFlow<List<XyAlbum>> get() = _recentAlbums
    //最多播放专辑
    val mostPlayedAlbums: StateFlow<List<XyAlbum>> get() = _mostPlayedAlbums
    //推荐音乐
    val recommendedMusic: StateFlow<List<XyMusic>> get() = _recommendedMusic
    //播放列表
    val playlists: StateFlow<List<XyAlbum>> get() = _playlists
    val dataCount: StateFlow<XyDataCount?> get() = _dataCount


    suspend fun initData() {
        loadOnce()
    }

    /**
     * 执行登录状态数据监听
     */
    suspend fun initLoginChangeMonitor() {
        observeFlows()
    }


    private suspend fun loadOnce() = coroutineScope {
        Log.i("HomeDataRepository", "loadOnce1 ${TokenServer.baseUrl}")
        launch {
            _mostPlayedMusic.value = db.musicDao.selectMaximumPlayMusicExtendList(20)
        }
        launch {
            _newestAlbums.value =
                db.albumDao.selectNewestList(20)
        }
        launch {
            _recentMusic.value = db.musicDao.selectPlayHistoryMusicExtendList(20)
        }
        launch {
            _recentAlbums.value =
                db.albumDao.selectPlayHistoryAlbumList(20)
        }
        launch {
            _mostPlayedAlbums.value =
                db.albumDao.selectMaximumPlayAlbumList(20)
        }
        launch {
            _recommendedMusic.value = db.musicDao.selectRecommendedMusicExtendList(20)
        }
        launch {
            _playlists.value =
                db.albumDao.selectPlaylist()
        }
        launch {
            _dataCount.value =
                db.dataCountDao.selectOne()
        }
    }

    private suspend fun observeFlows() = coroutineScope {
        launch {
            db.musicDao
                .selectLimitMusicListFlow(MusicDataTypeEnum.MAXIMUM_PLAY, 20)
                .distinctUntilChanged()
                .collect {
                    _mostPlayedMusic.value = it
                }
        }

        launch {
            db.albumDao
                .selectNewestListFlow(20)
                .distinctUntilChanged()
                .collect { _newestAlbums.value = it }
        }

        launch {
            db.musicDao
                .selectLimitMusicListFlow(MusicDataTypeEnum.PLAY_HISTORY, 20)
                .distinctUntilChanged()
                .collect {
                    _recentMusic.value = it
                }
        }

        launch {
            db.albumDao
                .selectPlayHistoryAlbumListFlow(20)
                .distinctUntilChanged()
                .collect { _recentAlbums.value = it }
        }

        launch {
            db.albumDao
                .selectMaximumPlayAlbumListFlow(20)
                .distinctUntilChanged()
                .collect {
                    _mostPlayedAlbums.value = it
                }
        }

        launch {
            db.musicDao
                .selectRecommendedMusicExtendListFlow(20)
                .distinctUntilChanged()
                .collect {
                    _recommendedMusic.value = it
                }
        }

        launch {
            db.albumDao
                .selectPlaylistFlow()
                .distinctUntilChanged()
                .collect { _playlists.value = it }
        }

        launch {
            db.dataCountDao
                .selectOneFlow()
                .distinctUntilChanged()
                .collect { _dataCount.value = it }
        }
    }
}
