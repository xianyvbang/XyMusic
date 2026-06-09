package cn.xybbz.ui.components

import androidx.compose.runtime.Immutable
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import org.jetbrains.compose.resources.StringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.empty_info
import xymusic_kmp.composeapp.generated.resources.loading
import xymusic_kmp.composeapp.generated.resources.reached_bottom

/**
 * Paging 在界面层使用的统一状态模型。
 *
 * 将分页组件中分散的 `refresh`、`append`、空数据和错误判断统一收敛，
 * 让列表、网格和下拉刷新组件都基于同一套语义渲染 UI。
 */
@Immutable
data class PagingUiState(
    /** 是否处于首次加载阶段，通常此时还没有任何列表数据。 */
    val isInitialLoading: Boolean,

    /** 是否正在执行刷新，包含首次加载和用户主动刷新。 */
    val isRefreshing: Boolean,

    /** 是否已经确认当前没有任何数据可展示。 */
    val isEmpty: Boolean,

    /** 是否正在执行加载更多。 */
    val isAppending: Boolean,

    /** 是否已经到达分页底部，不再继续追加数据。 */
    val isAppendEndReached: Boolean,

    /** 当前是否已经持有可展示的数据。 */
    val hasData: Boolean,

    /** 刷新阶段是否发生异常。 */
    val refreshError: Boolean,

    /** 加载更多阶段是否发生异常。 */
    val appendError: Boolean,
) {
    /** 是否展示下拉刷新的顶部刷新指示器，仅在已有数据时刷新展示。 */
    val shouldShowPullRefreshIndicator: Boolean
        get() = /*hasData &&*/ isRefreshing

    /** 是否展示底部加载中的状态，仅在首屏加载或加载更多时展示。 */
    val shouldShowBottomLoading: Boolean
        get() = isInitialLoading || (/*hasData &&*/ isAppending)

    /** 根据当前状态映射出的底部提示文案资源。 */
    val bottomText: StringResource
        get() = when {
            isEmpty -> Res.string.reached_bottom
            isInitialLoading || isAppending -> Res.string.loading
            isAppendEndReached -> Res.string.reached_bottom
            else -> Res.string.empty_info
        }
}

/**
 * 将 `LazyPagingItems` 转换为界面层使用的 [PagingUiState]。
 *
 * 这里会统一计算首次加载、刷新、空数据、加载更多、分页结束和错误状态，
 * 供列表/网格组件直接消费，避免在各个 UI 组件中重复编写 `loadState` 判断。
 *
 * @param treatNullAsInitialLoading 当 `LazyPagingItems` 为空时，是否按“首次加载中”处理。
 * 常用于分页对象尚未准备完成、但界面希望先展示加载态的场景。
 */
internal fun <T : Any> LazyPagingItems<T>?.toPagingUiState(
    treatNullAsInitialLoading: Boolean = false
): PagingUiState {
    if (this == null) {
        return PagingUiState(
            isInitialLoading = treatNullAsInitialLoading,
            isRefreshing = false,
            isEmpty = false,
            isAppending = false,
            isAppendEndReached = false,
            hasData = false,
            refreshError = false,
            appendError = false,
        )
    }

    val combinedLoadStates = loadState
    val hasData = itemCount > 0
    val refreshState = combinedLoadStates.refresh
    val appendState = combinedLoadStates.append
    val isIdle = combinedLoadStates.isIdle
    val hasError = combinedLoadStates.hasError

    return PagingUiState(
        isInitialLoading = refreshState is LoadState.Loading && !hasData,
        isRefreshing = refreshState is LoadState.Loading && hasData,
        isEmpty = isIdle && !hasError && !hasData,
        isAppending = appendState is LoadState.Loading && hasData,
        isAppendEndReached = hasData && appendState.endOfPaginationReached,
        hasData = hasData,
        refreshError = refreshState is LoadState.Error,
        appendError = appendState is LoadState.Error && hasData,
    )
}
