package cn.xybbz.extension

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow

@Composable
fun LazyListState.OnBottomReached(buffer: Int = 0, loadMore: () -> Unit) {

    require(buffer >= 0) { "buffer 值必须是正整数" }

    //是否应该加载更多的状态
    val shouldLoadMore = remember {
        //由另一个状态计算派生
        derivedStateOf {
            //获取最后显示的 item
            val lastVisibleItem =
                layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf true

            //如果最后显示的 item 是最后一个 item 的话,说明已经触底，需要加载更多

            lastVisibleItem.index == layoutInfo.totalItemsCount - 1 - buffer
        }
    }

    LaunchedEffect(shouldLoadMore) {
        //详见文档：https://developer.android.com/jetpack/compose/side-effects#snapshotFlow
        snapshotFlow { shouldLoadMore.value }.collect {
            if (it) {
                loadMore()
            }
        }

    }
}

/**
 * 判断列表属性是否在屏幕中
 */
@Composable
fun LazyListState.OnScreenDetection(index: Int = 0, loadMore: () -> Unit) {

    require(index >= 0) { "buffer 值必须是正整数" }
    //如果索引大于当前的index则表示列表需要移动

    //是否应该加载更多的状态
    val shouldLoadMore = remember {
        //由另一个状态计算派生
        derivedStateOf {
            //获取最后显示的 item
            val lastVisibleItem =
                layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf true

            //如果最后显示的 item 是最后一个 item 的话,说明已经触底，需要加载更多

            index > lastVisibleItem.index
        }
    }

    LaunchedEffect(shouldLoadMore) {
        //详见文档：https://developer.android.com/jetpack/compose/side-effects#snapshotFlow
        snapshotFlow { shouldLoadMore.value }.collect {
            if (it) {
                loadMore()
            }
        }

    }
}

/**
 * 向上滑动的时候
 */
@Composable
fun LazyListState.OnBottomTran(/*loadMore: (Boolean) -> Unit,*/ upMove: () -> Unit) {

//    var ifInit = true
    var nowFirstVisibleItemScrollOffset = 0

    val shouldLoadBackMore = remember {
        //由另一个状态计算派生
        derivedStateOf {
            //向上滑动
            firstVisibleItemScrollOffset > nowFirstVisibleItemScrollOffset
        }
    }

    LaunchedEffect(isScrollInProgress) {

        //详见文档：https://developer.android.com/jetpack/compose/side-effects#snapshotFlow
        snapshotFlow { shouldLoadBackMore.value }.collect {
            if (it) {
                nowFirstVisibleItemScrollOffset = firstVisibleItemScrollOffset
                //向上滑动的话就要执行方法
                upMove()
            }
        }

    }
}

//@OptIn(ExperimentalMaterial3Api::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetState.OnOffset() {


    val requireOffset = requireOffset()
    /*val shouldLoadBackMore = remember {
        //由另一个状态计算派生
        derivedStateOf {
            //向上滑动
            firstVisibleItemScrollOffset > nowFirstVisibleItemScrollOffset
        }
    }*/

    LaunchedEffect(requireOffset) {

        //详见文档：https://developer.android.com/jetpack/compose/side-effects#snapshotFlow
        snapshotFlow { requireOffset }.collect {
        }

    }
}

@Composable
fun ScrollState.OnScreenEnd(loadMore: (() -> Unit)) {
    val shouldLoadBackMore = remember {
        //由另一个状态计算派生
        derivedStateOf {
            //向上滑动
            !canScrollForward
        }
    }

    LaunchedEffect(shouldLoadBackMore) {
        //详见文档：https://developer.android.com/jetpack/compose/side-effects#snapshotFlow
        snapshotFlow { shouldLoadBackMore.value }.collect {
            if (it) {
                loadMore()
            }
        }

    }
}


/**
 * Checks if [index] is in the sticking position, that is, it's the first visible item and its
 * offset is equal to the content padding.
 */
fun LazyListState.isSticking(index: Int): State<Boolean> {
    return derivedStateOf {
        val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()
        firstVisible?.index == index && firstVisible.offset == -layoutInfo.beforeContentPadding
    }
}
