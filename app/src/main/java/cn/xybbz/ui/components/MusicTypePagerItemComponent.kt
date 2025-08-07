package cn.xybbz.ui.components


import android.util.Log
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import cn.xybbz.viewmodel.MusicTypePagerItemViewModel
import kotlinx.coroutines.flow.Flow

/**
 * 分类pager的item信息
 * @param [typeId] 类型id
 * @param [musicTypePagerItemViewModel] viewModel
 */
@Composable
fun <T : Any> MusicTypePagerItemComponent(
    onCollectAsLazyPagingItems: () -> Flow<PagingData<T>>?,
    tmpIndex: Int,
    musicTypePagerItemViewModel: MusicTypePagerItemViewModel<T> = viewModel(
        key = tmpIndex.toString()
    ),
    //当前所在页
    onTabIndexNowList: () -> List<Int>,
    listContent: LazyListScope.(collectAsLazyPagingItems: LazyPagingItems<T>) -> Unit,
    ifClearData: Boolean = false
) {
    val thisIndex by remember {
        Log.i("=====", "数据有变化吗${tmpIndex}")
        mutableIntStateOf(tmpIndex)
    }
    if (onTabIndexNowList().contains(thisIndex)) {
        val collectAsLazyPagingItems =
            musicTypePagerItemViewModel.getPagerFlow(onCollectAsLazyPagingItems, ifClearData)
                ?.collectAsLazyPagingItems()
        collectAsLazyPagingItems?.let {
            SwipeRefreshListComponent(collectAsLazyPagingItems = collectAsLazyPagingItems) {
                listContent(collectAsLazyPagingItems)
            }
        }

    }
}