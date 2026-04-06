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
import cn.xybbz.api.converter.jsonSerializer
import cn.xybbz.common.enums.DownloadTypes
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.PlayerTypeEnum
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LocalViewModel(
    val db: LocalDatabaseClient,
    val downloadDb: DownloadDatabaseClient,
    private val dataSourceManager: DataSourceManager,
    val musicController: MusicCommonController,
    val musicPlayContext: MusicPlayContext
) : ViewModel() {

    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    private val downloadMusicTasks: StateFlow<List<XyDownload>> =
        downloadDb.downloadDao.getAllMusicTasksFlow(
            notTypeData = DownloadTypes.APK.toString(),
            status = DownloadStatus.COMPLETED,
            mediaLibraryId = dataSourceManager.getConnectionId().toString()
        )
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val musicDownloadInfo: StateFlow<List<XyMusic>> =
        downloadMusicTasks
            .map { downloadList ->
                downloadList.mapNotNull { it.toLocalMusic() }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun musicList(
        onMusicPlayParameter: OnMusicPlayParameter,
        downloadList: List<XyMusic>,
        playerTypeEnum: PlayerTypeEnum? = null
    ) {
        viewModelScope.launch {
            val downloadPathMap = downloadMusicTasks.value.associate { it.uid to it.filePath }

            musicPlayContext.musicList(
                onMusicPlayParameter,
                downloadList.map { music ->
                    music.toPlayMusic().copy(
                        ifFavoriteStatus = db.musicDao.selectIfFavoriteByMusic(music.itemId),
                        filePath = downloadPathMap[music.itemId]
                    )
                },
                playerTypeEnum
            )
        }
    }

    private fun XyDownload.toLocalMusic(): XyMusic? {
        return data?.let {
            jsonSerializer.decodeFromString<XyMusic>(it)
        }
    }
}
