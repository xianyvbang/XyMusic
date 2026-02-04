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
import androidx.paging.PagingData
import androidx.paging.cachedIn
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.data.era.XyEraItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

abstract class PageListViewModel<T: Any>(dataSourceManager: DataSourceManager) : ViewModel() {

    private val _sortType = MutableStateFlow(Sort())

    val sortBy: StateFlow<Sort> = _sortType.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val listPage: Flow<PagingData<T>> =
        dataSourceManager.loginStateEvent
            .flatMapLatest {
                getFlowPageData(sortBy)
            }
            .cachedIn(viewModelScope) // 只调用一次


    /**
     * 设置排序配型
     */
    suspend fun setSortedData(
        sortType: SortTypeEnum?,
        refreshPage: suspend () -> Unit
    ) {
        val sort = this._sortType.value
        sort.sortType = sortType
        updateSort(sort.copy(), refreshPage = refreshPage)
    }

    /**
     * 设置年代筛选
     */
    suspend fun setFilterEraType(
        eraItem: XyEraItem,
        refreshPage: suspend () -> Unit
    ) {
        val yearList = eraItem.years
        val sort = this._sortType.value
        if (sort.yearList == yearList) {
            sort.yearList = null
        } else {
            sort.yearList = yearList
        }
        updateSort(sort.copy(), refreshPage = refreshPage)
    }

    /**
     * 设置年份筛选
     */
    suspend fun setFilterYear(
        yearList: List<Int>?,
        refreshPage: suspend () -> Unit
    ) {
        val sort = this._sortType.value
        sort.yearList = yearList
        updateSort(sort.copy(), refreshPage = refreshPage)
    }

    /**
     * 设置收藏过滤
     */
    suspend fun setFavorite(isFavorite: Boolean, refreshPage: suspend () -> Unit) {
        val sort = this._sortType.value
        sort.isFavorite = if (isFavorite) true else null
        updateSort(sort.copy(), refreshPage = refreshPage)
    }

    suspend fun updateSort(
        sort: Sort,
        refreshPage: suspend () -> Unit
    ) {
        this._sortType.update {
            sort
        }
        refreshPage()
    }

    suspend fun clearFilterOrSort(refreshPage: suspend () -> Unit){
        val sort = Sort()
        updateSort(sort, refreshPage = refreshPage)
    }

    /**
     * 获得数据结构
     */
    abstract fun getFlowPageData(sortFlow: StateFlow<Sort>): Flow<PagingData<T>>
}