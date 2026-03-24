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
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.utils.Log
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.entity.data.ext.toPlayerMusic
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyMusicExtend
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.lyrics_tab
import xymusic_kmp.composeapp.generated.resources.recommend
import xymusic_kmp.composeapp.generated.resources.song_tab

@KoinViewModel
class MusicPlayerViewModel (
    val musicController: MusicCommonController,
    val dataSourceManager: DataSourceManager,
    val downloadCacheController: DownloadCacheController,
    val lrcServer: LrcServer,
    private val db: DatabaseClient
) : ViewModel() {

    var fontSize by mutableFloatStateOf(1.0f)

    val dataList = listOf(Res.string.song_tab, Res.string.lyrics_tab, Res.string.recommend)

    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    fun addNextPlayer(musicExtend: XyMusicExtend) {
        viewModelScope.launch {
            Log.i("=====", "添加到列表")
            db.musicDao.save(musicExtend.music)
        }
        musicController.addNextPlayer(musicExtend.toPlayerMusic())
    }

}
