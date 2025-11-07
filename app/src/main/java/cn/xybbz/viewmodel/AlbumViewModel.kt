package cn.xybbz.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.localdata.data.album.XyAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AlbumViewModel @Inject constructor(
     val dataSourceManager: IDataSourceManager,
     val connectionConfigServer: ConnectionConfigServer,
     val backgroundConfig: BackgroundConfig
) : PageListViewModel() {


    /**
     * 首页专辑信息
     */

    //所以设置 initialLoadSize 的大小要占满一页,并且数据大小不能大于缓存数量
    //相关资料 https://issuetracker.google.com/issues/243851380

    val albumPageList: Flow<PagingData<XyAlbum>> = connectionConfigServer.loginStateFlow
        .flatMapLatest { loggedIn ->
            if (loggedIn) {
                dataSourceManager
                    .selectAlbumFlowList(_sortType)
                    .distinctUntilChanged()
                    .cachedIn(viewModelScope)
            } else {
                emptyFlow()
            }
        }.cachedIn(viewModelScope)
}