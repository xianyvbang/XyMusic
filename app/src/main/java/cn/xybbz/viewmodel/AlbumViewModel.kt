package cn.xybbz.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.entity.data.Sort
import cn.xybbz.config.IDataSourceManager
import cn.xybbz.localdata.data.era.XyEraItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val dataSourceManager: IDataSourceManager,
    private val connectionConfigServer: ConnectionConfigServer,
) : ViewModel() {


    private val _sortType = MutableStateFlow(Sort())

    val sortBy: StateFlow<Sort> = _sortType

    /**
     * 首页专辑信息
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    var albumPageList =
        connectionConfigServer.loginStateFlow.flatMapLatest { bool ->
            if (bool) {
                _sortType.flatMapLatest { sortBy ->
                    dataSourceManager.selectAlbumFlowList(
                        sortBy.sortType,
                        sortBy.isFavorite,
                        sortBy.eraItem?.years
                    ).distinctUntilChanged()
                }
            } else {
                flow { }
            }
        }.distinctUntilChanged().cachedIn(viewModelScope)



    /**
     * 设置排序类型
     */
    fun setSortedData(sortType: SortTypeEnum? = null) {
        this._sortType.update {
            it.sortType = sortType
            it.copy()
        }
    }

    fun setFilterEraType(eraItem: XyEraItem) {
        this._sortType.update {
            if (it.eraItem == eraItem) {
                it.eraItem = null
            } else {
                it.eraItem = eraItem
            }
            it.copy()
        }
    }

    fun setFavorite(isFavorite: Boolean) {
        Log.i("=====", "数据变化$isFavorite")
        this._sortType.update {
            it.isFavorite = isFavorite
            it.copy()
        }
    }
}