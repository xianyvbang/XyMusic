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
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.select.SelectControl
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.entity.data.Sort
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.HomeMusic
import cn.xybbz.localdata.data.music.XyMusic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MusicViewModel @Inject constructor(
    val dataSourceManager: DataSourceManager,
    val db: DatabaseClient,
    val settingsManager: SettingsManager,
    val musicPlayContext: MusicPlayContext,
    val musicController: MusicController,
    val selectControl: SelectControl,
    val backgroundConfig: BackgroundConfig
) : PageListViewModel<HomeMusic>(dataSourceManager, SortTypeEnum.MUSIC_NAME_ASC) {

    val downloadMusicIdsFlow =
        db.downloadDao.getAllMusicTaskUidsFlow()
    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    suspend fun getMusicInfoById(musicId: String): XyMusic? = db.musicDao.selectById(musicId)

    /**
     * 获得数据结构
     */
    override fun getFlowPageData(sort: Sort): Flow<PagingData<HomeMusic>> {
        return dataSourceManager.selectMusicFlowList(sort)
    }

    override suspend fun updateDataSourceRemoteKey() {
        dataSourceManager.updateDataSourceRemoteKey(RemoteIdConstants.MUSIC)
    }
}