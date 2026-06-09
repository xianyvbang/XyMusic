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

package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import cn.xybbz.ui.common.UiConstants.MusicCardImageSize
import cn.xybbz.ui.theme.XyTheme
import com.github.panpf.sketch.ability.bindPauseLoadWhenScrolling
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.reached_bottom

/**
 * LazyVerticalGrid组件抽取-可以拖拽
 *
 * @param [modifier] 修饰语
 * @param [paddingValues] 填充值
 * @param [collectAsLazyPagingItems] 分页数据
 * @param [content] 内容
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> SwipeRefreshVerticalGridListComponent(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    collectAsLazyPagingItems: LazyPagingItems<T>,
    bottomItem: (LazyGridScope.() -> Unit)? = null,
    content: LazyGridScope.(Boolean) -> Unit
) {
    val pagingUiState = collectAsLazyPagingItems.toPagingUiState()

    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        state = state,
        isRefreshing = pagingUiState.shouldShowPullRefreshIndicator,
        onRefresh = {
            collectAsLazyPagingItems.refresh()
        }
    ) {
        LazyVerticalGridComponent(
            modifier = modifier,
            pageListItems = collectAsLazyPagingItems,
            lazyGridState = lazyGridState,
            pagingUiState = pagingUiState,
            bottomItem = bottomItem,
            content = {
                content.invoke(this, pagingUiState.isRefreshing)
            }
        )
    }

}


@Composable
fun <T : Any> VerticalGridListComponent(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    collectAsLazyPagingItems: LazyPagingItems<T>,
    bottomItem: (LazyGridScope.() -> Unit)? = null,
    content: LazyGridScope.() -> Unit
) {
    val pagingUiState = collectAsLazyPagingItems.toPagingUiState()
    LazyVerticalGridComponent(
        modifier = modifier,
        pageListItems = collectAsLazyPagingItems,
        lazyGridState = lazyGridState,
        pagingUiState = pagingUiState,
        bottomItem = bottomItem,
        content = content
    )

}

/**
 * LazyVerticalGrid组件抽取
 */
@Composable
fun <T : Any> LazyVerticalGridComponent(
    modifier: Modifier = Modifier,
    pageListItems: LazyPagingItems<T>?,
    lazyGridState: LazyGridState,
    pagingUiState: PagingUiState,
    bottomItem: (LazyGridScope.() -> Unit)? = null,
    content: LazyGridScope.() -> Unit
) {

    LazyVerticalGridComponent(
        modifier = modifier,
        lazyGridState = lazyGridState,
        bottomItem = bottomItem,
    ) {
        content()
        lazyColumBottomComponent(
            pagingUiState = pagingUiState,
            items = pageListItems
        )


    }
}

@Composable
fun LazyVerticalGridComponent(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    columns: GridCells = GridCells.Adaptive(MusicCardImageSize),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.outerHorizontalPadding,
        vertical = XyTheme.dimens.outerVerticalPadding
    ),
    verticalArrangement: Arrangement.Vertical =
        Arrangement.spacedBy(XyTheme.dimens.innerVerticalPadding),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(
        XyTheme.dimens.outerVerticalPadding /*VERTICAL_INTERNAL_DP.dp*/,
        Alignment.CenterHorizontally
    ),
    bottomItem: (LazyGridScope.() -> Unit)? = {
        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyLoadingAndStatus(
                text = stringResource(Res.string.reached_bottom),
                ifLoading = false
            )
        }
    },
    content: LazyGridScope.() -> Unit
) {

    bindPauseLoadWhenScrolling(lazyGridState)
    PlatformLazyVerticalGridComponent(
        lazyGridState = lazyGridState,
        columns = columns,
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        contentPadding = contentPadding,
        content = {
            content()
            bottomItem?.invoke(this)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(
                    modifier = Modifier.height(
                        XyTheme.dimens.snackBarPlayerHeight
                    )
                )
            }
        }
    )
}

@Composable
internal expect fun PlatformLazyVerticalGridComponent(
    modifier: Modifier,
    lazyGridState: LazyGridState,
    columns: GridCells,
    contentPadding: PaddingValues,
    verticalArrangement: Arrangement.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    content: LazyGridScope.() -> Unit
)

/**
 * lazyColum底部组件集合
 * refresh多次加载的问题-> https://issuetracker.google.com/issues/287882996
 * 惰性列底部组件
 * @param [retry] 重试
 * @param [onHasLoadingDone] 是否第一次加载
 * @param [onPagingItems] 关于家庭音乐寻呼项目
 */
fun <T : Any> LazyGridScope.lazyColumBottomComponent(
    retry: (() -> Unit)? = null,
    pagingUiState: PagingUiState,
    items: LazyPagingItems<T>?
) {

    item(span = { GridItemSpan(maxLineSpan) }) {
        LazyLoadingAndStatus(
            text = stringResource(pagingUiState.bottomText),
            ifLoading = pagingUiState.shouldShowBottomLoading
        )
    }

    when {
        pagingUiState.refreshError -> {
            item(key = 7777, span = { GridItemSpan(maxLineSpan) }) {
                if (!pagingUiState.hasData) {
                    //刷新的时候，如果itemCount小于0，第一次加载异常

                    ErrorContent {
                        items?.retry()
                    }
                } else {
                    ErrorMoreRetryItem {
                        items?.retry()
                    }
                }
            }
        }

        pagingUiState.appendError -> {
            item(span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }) {
                ErrorMoreRetryItem {
                    if (retry == null)
                        items?.retry()
                    else
                        retry.invoke()
                }
            }
        }
    }

}
