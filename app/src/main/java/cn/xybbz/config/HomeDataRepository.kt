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

import android.util.Log
import cn.xybbz.api.TokenServer
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.count.XyDataCount
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class HomeDataRepository(
    private val db: DatabaseClient,
    private val dataSourceManager: DataSourceManager
) {

    private val _mostPlayedMusic = MutableStateFlow<List<XyMusicExtend>>(emptyList())
    private val _newestAlbums = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _recentMusic = MutableStateFlow<List<XyMusicExtend>>(emptyList())
    private val _recentAlbums = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _mostPlayedAlbums = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _recommendedMusic = MutableStateFlow<List<XyMusicExtend>>(emptyList())
    private val _playlists = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _dataCount = MutableStateFlow<XyDataCount?>(null)

    val mostPlayedMusic: StateFlow<List<XyMusicExtend>> get() = _mostPlayedMusic
    val newestAlbums: StateFlow<List<XyAlbum>> get() = _newestAlbums
    val recentMusic: StateFlow<List<XyMusicExtend>> get() = _recentMusic
    val recentAlbums: StateFlow<List<XyAlbum>> get() = _recentAlbums
    val mostPlayedAlbums: StateFlow<List<XyAlbum>> get() = _mostPlayedAlbums
    val recommendedMusic: StateFlow<List<XyMusicExtend>> get() = _recommendedMusic
    val playlists: StateFlow<List<XyAlbum>> get() = _playlists
    val dataCount: StateFlow<XyDataCount?> get() = _dataCount


   suspend fun initData(){
        loadOnce()
    }

    /**
     * 执行登录状态数据监听
     */
    suspend fun initLoginChangeMonitor(){
        observeFlows()
    }


    private suspend fun loadOnce() = coroutineScope {
        Log.d("HomeDataRepository", "loadOnce1 ${TokenServer.baseUrl}")
        launch {
            _mostPlayedMusic.value =
                db.musicDao.selectMaximumPlayMusicExtendList(20)
        }
        launch {
            _newestAlbums.value =
                db.albumDao.selectNewestList(20)
        }
        launch {
            _recentMusic.value =
                db.musicDao.selectPlayHistoryMusicExtendList(20)
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
            _recommendedMusic.value =
                db.musicDao.selectRecommendedMusicExtendList(20)
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

    private suspend fun observeFlows() =coroutineScope{
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
                .collect { _recentMusic.value = it }
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