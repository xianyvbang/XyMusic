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
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.data.album.XyAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AlbumViewModel @Inject constructor(
    val dataSourceManager: DataSourceManager,
    val backgroundConfig: BackgroundConfig
) : PageListViewModel<XyAlbum>(dataSourceManager) {


    /**
     * 首页专辑信息
     */

    //所以设置 initialLoadSize 的大小要占满一页,并且数据大小不能大于缓存数量
    //相关资料 https://issuetracker.google.com/issues/243851380

    /**
     * 获得数据结构
     */
    override fun getFlowPageData(sortFlow: StateFlow<Sort>): Flow<PagingData<XyAlbum>> {
        return dataSourceManager.selectAlbumFlowList(sortFlow)
    }

}