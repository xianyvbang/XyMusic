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

import androidx.paging.PagingData
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.common.enums.DownloadTypes
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.select.SelectControl
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class MusicViewModel(
    val dataSourceManager: DataSourceManager,
    val db: LocalDatabaseClient,
    val downloadDb: DownloadDatabaseClient,
    val settingsManager: SettingsManager,
    val musicPlayContext: MusicPlayContext,
    val musicController: MusicCommonController,
    val selectControl: SelectControl,
) : PageListViewModel<XyMusic>(dataSourceManager, null) {

    val downloadMusicIdsFlow =
        downloadDb.downloadDao.getAllMusicTaskUidsFlow(
            notTypeData = DownloadTypes.APK.toString(),
            mediaLibraryId = dataSourceManager.getConnectionId().toString()
        )
    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    suspend fun getMusicInfoById(musicId: String): XyMusic? = db.musicDao.selectById(musicId)

    /**
     * 获得数据结构
     */
    override fun getFlowPageData(sort: Sort): Flow<PagingData<XyMusic>> {
        return dataSourceManager.selectMusicFlowList(sort)
    }

    override suspend fun updateDataSourceRemoteKey() {
        dataSourceManager.updateDataSourceRemoteKey(RemoteIdConstants.MUSIC)
    }
}
