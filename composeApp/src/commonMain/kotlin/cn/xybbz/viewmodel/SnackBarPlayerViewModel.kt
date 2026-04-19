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
import androidx.room.Transaction
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.client.FavoriteCoordinator
import cn.xybbz.api.converter.jsonSerializer
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.music.DownloadCacheCommonController
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.select.SelectControl
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.core.DownloadRequest
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.common.enums.getDownloadType
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_download_list

@KoinViewModel
class SnackBarPlayerViewModel(
    val musicController: MusicCommonController,
    val db: LocalDatabaseClient,
    val downloadDb: DownloadDatabaseClient,
    private val downloadCacheController: DownloadCacheCommonController,
    val dataSourceManager: DataSourceManager,
    val selectControl: SelectControl,
    private val downloaderManager: DownloaderManager
) : ViewModel() {


    /**
     * 清空播放列表
     */
    @Transaction
    suspend fun clearPlayer() {
        db.playerDao.removeByDatasource()
        db.musicDao.removePlayQueueMusic()
        viewModelScope.launch {
            downloadCacheController.clearCache()
        }
    }

    fun downloadMusics() {
        viewModelScope.launch {
            val downloadTypes =
                getDownloadType(dataSourceManager.dataSourceType)
            val itemIds = selectControl.selectMusicIdList.toList()
            val musicList = db.musicDao.selectByIds(itemIds)
            val requests = musicList.map { musicData ->
                DownloadRequest(
                    url = musicData.downloadUrl,
                    fileName = musicData.name + "." + musicData.container,
                    fileSize = musicData.size ?: 0,
                    uid = musicData.itemId,
                    title = musicData.name,
                    type = downloadTypes.toString(),
                    cover = musicData.pic,
                    duration = musicData.runTimeTicks,
                    mediaLibraryId = dataSourceManager.getConnectionId().toString(),
                    data = jsonSerializer.encodeToString(musicData)
                )
            }
            if (requests.isNotEmpty()) {
                downloaderManager.enqueue(
                    *requests.toTypedArray()
                )
                MessageUtils.sendPopTip(Res.string.add_download_list)
            }
        }.invokeOnCompletion { selectControl.dismiss() }

    }

    suspend fun toggleFavorite(
        itemId: String,
        ifFavorite: Boolean
    ): Boolean {
        // Snackbar 直接复用现有收藏切换逻辑，保持和其他页面行为一致。
        return FavoriteCoordinator.setFavoriteData(
            dataSourceManager = dataSourceManager,
            type = MusicTypeEnum.MUSIC,
            itemId = itemId,
            ifFavorite = ifFavorite,
            musicController = musicController
        )
    }

    suspend fun loadMusicInfo(itemId: String): XyMusic? {
        // 优先走数据源详情，拿不到时再回退本地库，避免信息按钮无响应。
        val remoteMusicInfo = runCatching {
            dataSourceManager.selectMusicInfoById(itemId)
        }.getOrNull()

        return remoteMusicInfo ?: runCatching {
            db.musicDao.selectById(itemId)
        }.getOrNull()
    }
}
