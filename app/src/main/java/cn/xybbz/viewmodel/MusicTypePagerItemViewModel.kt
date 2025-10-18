package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class MusicTypePagerItemViewModel<T : Any> : ViewModel() {

    // 分页数据源
    private var pagingDataFlow: Flow<PagingData<T>>? = null


    fun getPagerFlow(
        loadPagingDataFlowBlock: () -> Flow<PagingData<T>>?,
        ifClearData: Boolean
    ): Flow<PagingData<T>>? {
        if (ifClearData) {
            pagingDataFlow = null
        }
        if (pagingDataFlow == null) {
            pagingDataFlow = loadPagingDataFlowBlock.invoke()
        }
        return pagingDataFlow
    }
}