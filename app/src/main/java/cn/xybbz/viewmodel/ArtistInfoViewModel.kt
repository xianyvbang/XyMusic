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
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.select.SelectControl
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.artist.XyArtist
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * 艺术家详情ViewModel
 */
@HiltViewModel(assistedFactory = ArtistInfoViewModel.Factory::class)
class ArtistInfoViewModel @AssistedInject constructor(
    @Assisted private val artistId: String,
    val dataSourceManager: DataSourceManager,
    val musicPlayContext: MusicPlayContext,
    val musicController: MusicController,
    val backgroundConfig: BackgroundConfig,
    val db: DatabaseClient,
    val selectControl: SelectControl
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(artistId: String): ArtistInfoViewModel
    }

    val downloadMusicIdsFlow =
        db.downloadDao.getAllMusicTaskUidsFlow()
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
     * 艺术家描述信息2
     */
    var artistDescribe2 by mutableStateOf<String?>(null)
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
        dataSourceManager.loginStateEvent
            .flatMapLatest {
                dataSourceManager.selectMusicListByArtistId(artistId).distinctUntilChanged()
            }
            .cachedIn(viewModelScope)


    //艺术家的专辑列表
    @OptIn(ExperimentalCoroutinesApi::class)
    val albumList =
        dataSourceManager.loginStateEvent
            .flatMapLatest {
                dataSourceManager.selectAlbumListByArtistId(artistId).distinctUntilChanged()
            }
            .cachedIn(viewModelScope)

    /*@OptIn(ExperimentalCoroutinesApi::class)
    val resemblanceArtistList =
        dataSourceManager.loginStateEvent
            .flatMapLatest {
                dataSourceManager.getResemblanceArtist(artistId).distinctUntilChanged()
            }
            .cachedIn(viewModelScope)*/

    init {
        getArtistInfoData()
    }

    /**
     * 获得艺术家信息
     */
    private fun getArtistInfoData() {
        viewModelScope.launch {
            val artistInfoTmp = dataSourceManager.selectArtistInfoById(artistId)
            if (artistInfoTmp != null) {
                artistInfoData = artistInfoTmp
                ifFavorite = artistInfoTmp.ifFavorite
            }

            artistDescribe = artistInfoTmp?.describe
        }
        viewModelScope.launch {
            val artistInfo =
                dataSourceManager.getSimilarArtistsRemotely(artistId, 0, 12)
            artistDescribe2 =
                artistInfo?.describe
            resemblanceArtistList = artistInfo?.similarArtist ?: emptyList()
        }
    }

    /**
     * 更新收藏信息
     */
    fun updateFavorite(ifFavorite: Boolean) {
        this.ifFavorite = ifFavorite
    }
}