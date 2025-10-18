package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.data.era.XyEraItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

abstract class PageListViewModel : ViewModel() {

    protected val _sortType = MutableStateFlow(Sort())

    val sortBy: StateFlow<Sort> = _sortType


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
        val yearList = eraItem.years.split(Constants.ARTIST_DELIMITER).map { it.toInt() }
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
        sort.isFavorite = isFavorite
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
}