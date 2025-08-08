package cn.xybbz.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.IDataSourceManager
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.data.era.XyEraItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val dataSourceManager: IDataSourceManager,
    private val connectionConfigServer: ConnectionConfigServer,
) : ViewModel() {


    private val _sortType = MutableStateFlow(Sort())

    val sortBy: StateFlow<Sort> = _sortType


    // 将 login + sortType 合并成一个组合 flow
    private val pagingParamsFlow =
        combine(connectionConfigServer.loginStateFlow, sortBy) { login, sortType ->
            if (login) {
                sortType
            } else {
                null
            }
        }.filterNotNull() // 过滤掉未登录情况

    /**
     * 首页专辑信息
     */
//todo 知道为什么会刷新后会有append, 初始加载（基于 initialLoadSize）已加载所有缓存数据，nextKey = null则会触发 APPEND。
    //所以设置 initialLoadSize 的大小要占满一页,并且数据大小不能大于缓存数量
    //相关资料 https://issuetracker.google.com/issues/243851380
    //考虑加载中的时候禁止点击
    @OptIn(ExperimentalCoroutinesApi::class)
    val albumPageList= pagingParamsFlow
        .flatMapLatest  { sortType ->
            dataSourceManager.selectAlbumFlowList(
                sortType.sortType,
                sortType.isFavorite,
                sortType.eraItem?.years
            )
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