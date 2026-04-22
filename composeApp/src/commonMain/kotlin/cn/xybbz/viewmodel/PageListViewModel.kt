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
import cn.xybbz.common.enums.LoginStateType
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.entity.data.Sort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

abstract class PageListViewModel<T : Any>(
    dataSourceManager: DataSourceManager,
    val defaultSortType: SortTypeEnum? = null
) : ViewModel() {

    // 列表排序与筛选状态的唯一来源
    private val _sortType = MutableStateFlow(Sort(defaultSortType))
    // 列表排序与筛选状态的对外只读流
    val sortBy: StateFlow<Sort> = _sortType.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val listPage: Flow<PagingData<T>> =
        dataSourceManager.mergeFlow
            .flatMapLatest {
                getFlowPageData(
                    sortBy.value
                )
            }
            .cachedIn(viewModelScope)

    /*@OptIn(ExperimentalCoroutinesApi::class)
    val listPage: Flow<PagingData<T>> =
        dataSourceManager.loginStateFlow
            .flatMapLatest {
                Log.i("music数据变化","${sortBy.value.sortType}")
                getFlowPageData(sortBy)
            }
            .cachedIn(viewModelScope)*/ // 只调用一次


    /**
     * 设置排序配型
     */
    suspend fun setSortedData(
        sortType: SortTypeEnum?,
        refreshPage: suspend () -> Unit
    ) {
        updateSort(_sortType.value.copy(sortType = sortType), refreshPage = refreshPage)
    }

    /**
     * 设置年份筛选
     */
    suspend fun setFilterYear(
        yearList: List<Int>?,
        refreshPage: suspend () -> Unit
    ) {
        updateSort(_sortType.value.copy(yearList = yearList), refreshPage = refreshPage)
    }

    /**
     * 设置收藏过滤
     */
    suspend fun setFavorite(isFavorite: Boolean, refreshPage: suspend () -> Unit) {
        updateSort(
            _sortType.value.copy(isFavorite = if (isFavorite) true else null),
            refreshPage = refreshPage
        )
    }

    /**
     * 更新排序与筛选状态。
     */
    suspend fun updateSort(
        sort: Sort,
        refreshPage: suspend () -> Unit
    ) {
        updateDataSourceRemoteKey()
        this._sortType.update {
            sort
        }
//        refreshPage()
    }

    /**
     * 清空当前排序与筛选条件。
     */
    suspend fun clearFilterOrSort(refreshPage: suspend () -> Unit) {
        val sort = Sort(defaultSortType)
        updateSort(sort, refreshPage = refreshPage)
    }

    /**
     * 判断当前排序或筛选是否与默认状态不同。
     */
    fun isSortChange(): Boolean {
        return _sortType.value.sortType != defaultSortType || _sortType.value.isFavorite != null || _sortType.value.yearList != null
    }

    /**
     * 获得数据结构
     */
    abstract fun getFlowPageData(sort: Sort): Flow<PagingData<T>>

    /**
     * 更新分页远端键，确保筛选变更后重新拉取数据。
     */
    abstract suspend fun updateDataSourceRemoteKey()
}
