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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cn.xybbz.R
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.music.CacheController
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.localdata.data.music.XyMusic
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    val musicController: MusicController,
    val dataSourceManager: DataSourceManager,
    val favoriteRepository: FavoriteRepository,
    val cacheController: CacheController,
    val lrcServer: LrcServer
) : ViewModel() {

    var fontSize by mutableFloatStateOf(1.0f)

    val dataList = listOf(R.string.song_tab, R.string.lyrics_tab, R.string.recommend)

    var similarMusicList by mutableStateOf(emptyList<XyMusic>())
}