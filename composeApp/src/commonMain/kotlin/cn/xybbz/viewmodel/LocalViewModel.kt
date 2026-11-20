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
import cn.xybbz.assembler.MusicPlayAssembler
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.converter.jsonSerializer
import cn.xybbz.common.enums.DownloadTypes
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.utils.playableDownloadFilePath
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.PlayerModeEnum
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
    val musicPlayContext: MusicPlayContext,
    // 本地歌曲删除需要借助下载管理器同步清理下载记录和磁盘文件
    private val downloaderManager: DownloaderManager
) : ViewModel() {

    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    /**
     * 本地已下载歌曲对应的下载任务列表。
     *
     * 列表页需要按下载记录 id 做选择与删除，因此对外暴露原始任务，
     * 由页面自己映射音乐信息，避免同名歌曲或重复下载记录串选。
     */
    val downloadMusicTasks: StateFlow<List<XyDownload>> =
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
        playerModeEnum: PlayerModeEnum? = null
    ) {
        viewModelScope.launch {
            val downloadPathMap = downloadMusicTasks.value.mapNotNull { download ->
                playableDownloadFilePath(download.filePath)?.let { filePath -> download.uid to filePath }
            }.toMap()

            musicPlayContext.musicList(
                onMusicPlayParameter,
                downloadList.map { music ->
                    MusicPlayAssembler.toPlayMusic(
                        music = music,
                        ifFavoriteStatus = db.musicDao.selectIfFavoriteByMusic(music.itemId),
                        filePath = downloadPathMap[music.itemId]
                    )
                },
                playerModeEnum
            )
        }
    }

    private fun XyDownload.toLocalMusic(): XyMusic? {
        return data?.let {
            jsonSerializer.decodeFromString<XyMusic>(it)
        }
    }

    /**
     * 按下载记录 id 删除本地已下载歌曲，同时清理下载记录与磁盘上的音乐文件。
     *
     * @param downloadIds 需要删除的下载任务 id 列表，支持批量删除。
     */
    fun deleteLocalMusicByIds(downloadIds: List<Long>) {
        if (downloadIds.isEmpty()) {
            return
        }
        viewModelScope.launch {
            // 直接按下载记录 id 删除，文件与记录由下载调度器同步清理
            downloaderManager.deleteAndAwait(*downloadIds.toLongArray(), deleteFile = true)
        }
    }
}
