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
import androidx.paging.PagingData
import androidx.paging.cachedIn
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.genre.XyGenre
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = GenresInfoViewModel.Factory::class)
class GenresInfoViewModel @AssistedInject constructor(
    @Assisted private val genreId: String,
    private val dataSourceManager: DataSourceManager,
    val backgroundConfig: BackgroundConfig
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(genreId: String): GenresInfoViewModel
    }



    var genreInfo by mutableStateOf<XyGenre?>(null)
        private set


    init {
        getGenreInfo()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val albumList: Flow<PagingData<XyAlbum>> =
        dataSourceManager.mergeFlow
            .flatMapLatest {
                dataSourceManager.selectAlbumListByGenreId(genreId)
            }
            .cachedIn(viewModelScope)


    private fun getGenreInfo() {
        viewModelScope.launch {
            genreInfo = dataSourceManager.getGenreById(genreId)
        }
    }
}