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
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.enums.DownloadStatus
import cn.xybbz.localdata.enums.PlayerTypeEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalViewModel @Inject constructor(
    val db: DatabaseClient,
    val musicController: MusicController,
    val musicPlayContext: MusicPlayContext,
    val backgroundConfig: BackgroundConfig
) : ViewModel() {

    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    val musicDownloadInfo: StateFlow<List<XyDownload>> =
        db.downloadDao.getAllMusicTasksFlow(status = DownloadStatus.COMPLETED)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun musicList(
        onMusicPlayParameter: OnMusicPlayParameter,
        downloadList: List<XyDownload>,
        playerTypeEnum: PlayerTypeEnum? = null
    ) {
        viewModelScope.launch {
            musicPlayContext.musicList(onMusicPlayParameter, downloadList.mapNotNull {
                val playMusic = it.toPlayMusic()
                playMusic?.copy(
                    ifFavoriteStatus = db.musicDao.selectIfFavoriteByMusic(playMusic.itemId),
                    filePath = it.filePath
                )
            }, playerTypeEnum)
        }
    }
}