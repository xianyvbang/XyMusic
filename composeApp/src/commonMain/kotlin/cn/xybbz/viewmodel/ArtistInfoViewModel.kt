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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.DownloadTypes
import cn.xybbz.config.download.enqueueMusicDownload
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.select.SelectControl
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

/**
 * 艺术家详情ViewModel
 */
@KoinViewModel
class ArtistInfoViewModel(
    @InjectedParam private val artistId: String,
    @InjectedParam private val artistName: String,
    val dataSourceManager: DataSourceManager,
    val musicPlayContext: MusicPlayContext,
    val musicController: MusicCommonController,
    val db: LocalDatabaseClient,
    val downloadDb: DownloadDatabaseClient,
    val selectControl: SelectControl,
    private val downloaderManager: DownloaderManager,
) : ViewModel() {

    val downloadMusicIdsFlow =
        downloadDb.downloadDao.getAllMusicTaskUidsFlow(
            notTypeData = DownloadTypes.APK.toString(),
            mediaLibraryId = dataSourceManager.getConnectionId().toString()
        )
    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    /**
     * 艺术家信息
     */
    var artistInfoData by mutableStateOf<XyArtist?>(null)
        private set

    /**
     * 艺术家描述
     */
    var artistDescribe by mutableStateOf<String?>(null)
        private set

    /**
     * 相似艺术家
     */
    var resemblanceArtistList by mutableStateOf<List<XyArtist>>(emptyList())
        private set

    /**
     * 是否收藏
     */
    var ifFavorite by mutableStateOf(false)
        private set

    //艺术家的音乐列表
    @OptIn(ExperimentalCoroutinesApi::class)
    val musicList =
        dataSourceManager.loginStateFlow
            .flatMapLatest {
                dataSourceManager.selectMusicListByArtistId(artistId, artistName)
                    .distinctUntilChanged()
            }
            .cachedIn(viewModelScope)


    //艺术家的专辑列表
    @OptIn(ExperimentalCoroutinesApi::class)
    val albumList =
        dataSourceManager.loginStateFlow
            .flatMapLatest {
                dataSourceManager.selectAlbumListByArtistId(artistId).distinctUntilChanged()
            }
            .cachedIn(viewModelScope)

    init {
        getArtistInfoData()
        getSimilarArtistsRemotely()
    }

    /**
     * 获得艺术家信息
     */
    private fun getArtistInfoData() {
        viewModelScope.launch {
            val artistInfo = dataSourceManager.selectArtistInfoById(artistId)
            if (artistInfo != null && artistInfoData == null) {
                artistInfoData = artistInfo
                ifFavorite = artistInfo.ifFavorite
            }
            if (artistDescribe.isNullOrBlank())
                artistDescribe = artistInfo?.describe
        }

        viewModelScope.launch {
            val artistInfo = dataSourceManager.selectServerArtistInfo(artistId)
            if (artistInfo != null && artistInfoData == null) {
                artistInfoData = artistInfo
                ifFavorite = artistInfo.ifFavorite
            }
            if (artistDescribe.isNullOrBlank())
                artistDescribe = artistInfo?.describe
        }


    }

    fun getSimilarArtistsRemotely() {
        viewModelScope.launch {
            val similarArtists =
                dataSourceManager.getSimilarArtistsRemotely(artistId, 0, 12)
            resemblanceArtistList = similarArtists
        }
    }

    /**
     * 更新收藏信息
     */
    fun updateFavorite(ifFavorite: Boolean) {
        this.ifFavorite = ifFavorite
    }

    fun downloadMusic(musicData: XyMusic) {
        viewModelScope.launch {
            downloaderManager.enqueueMusicDownload(musicData, dataSourceManager)
        }
    }
}
