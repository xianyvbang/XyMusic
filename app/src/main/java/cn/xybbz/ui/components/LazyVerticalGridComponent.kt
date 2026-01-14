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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import cn.xybbz.R
import cn.xybbz.common.UiConstants.MusicCardImageSize
import cn.xybbz.ui.theme.XyTheme

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
    content: LazyGridScope.(Boolean) -> Unit
) {

    val isRefreshing by remember {
        derivedStateOf {
            collectAsLazyPagingItems.loadState.refresh is LoadState.Loading
        }
    }
    //是否加载中
    val isLoading by remember {
        derivedStateOf {
            (collectAsLazyPagingItems.loadState.refresh is LoadState.Loading
                    || (collectAsLazyPagingItems.loadState.mediator != null
                    && collectAsLazyPagingItems.loadState.mediator?.refresh is LoadState.Loading)
                    || collectAsLazyPagingItems.loadState.source.refresh is LoadState.Loading)
                    && collectAsLazyPagingItems.itemCount <= 0
        }
    }

    val ifNotData by remember {
        derivedStateOf {
            collectAsLazyPagingItems.loadState.refresh is LoadState.NotLoading
                    && ((collectAsLazyPagingItems.loadState.mediator != null
                    && collectAsLazyPagingItems.loadState.mediator?.refresh is LoadState.NotLoading)
                    || collectAsLazyPagingItems.loadState.mediator == null)
                    && collectAsLazyPagingItems.loadState.source.refresh is LoadState.NotLoading
                    && collectAsLazyPagingItems.loadState.prepend is LoadState.NotLoading
                    && collectAsLazyPagingItems.loadState.append is LoadState.NotLoading
                    && collectAsLazyPagingItems.itemCount <= 0
        }
    }


    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        state = state,
        isRefreshing = !ifNotData && !isLoading && isRefreshing,
        onRefresh = {
            collectAsLazyPagingItems.refresh()
        }
    ) {
        LazyVerticalGridComponent(
            modifier = modifier,
            pageListItems = collectAsLazyPagingItems,
            lazyGridState = lazyGridState,
            onIsLoading = { isLoading },
            onIfNotData = { ifNotData },
            content = {
                content.invoke(this, isRefreshing)
            }
        )
    }

}


@Composable
fun <T : Any> VerticalGridListComponent(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    collectAsLazyPagingItems: LazyPagingItems<T>,
    content: LazyGridScope.() -> Unit
) {

    //是否加载中
    val isLoading by remember {
        derivedStateOf {
            (collectAsLazyPagingItems.loadState.refresh is LoadState.Loading
                    || (collectAsLazyPagingItems.loadState.mediator != null
                    && collectAsLazyPagingItems.loadState.mediator?.refresh is LoadState.Loading)
                    || collectAsLazyPagingItems.loadState.source.refresh is LoadState.Loading)
                    && collectAsLazyPagingItems.itemCount <= 0
        }
    }

    val ifNotData by remember {
        derivedStateOf {
            collectAsLazyPagingItems.loadState.refresh is LoadState.NotLoading
                    && ((collectAsLazyPagingItems.loadState.mediator != null
                    && collectAsLazyPagingItems.loadState.mediator?.refresh is LoadState.NotLoading)
                    || collectAsLazyPagingItems.loadState.mediator == null)
                    && collectAsLazyPagingItems.loadState.source.refresh is LoadState.NotLoading
                    && collectAsLazyPagingItems.loadState.prepend is LoadState.NotLoading
                    && collectAsLazyPagingItems.loadState.append is LoadState.NotLoading
                    && collectAsLazyPagingItems.itemCount <= 0
        }
    }



    LazyVerticalGridComponent(
        modifier = modifier,
        pageListItems = collectAsLazyPagingItems,
        lazyGridState = lazyGridState,
        onIsLoading = { isLoading },
        onIfNotData = { ifNotData },
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
    onIsLoading: () -> Boolean,
    onIfNotData: () -> Boolean,
    content: LazyGridScope.() -> Unit
) {

    LazyVerticalGridComponent(
        modifier = modifier,
        lazyGridState = lazyGridState,
    ) {
        content()
        lazyColumBottomComponent(
            onIsLoading = onIsLoading,
            onIfNotData = onIfNotData,
            items = pageListItems
        )


    }
}

@Composable
fun LazyVerticalGridComponent(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    content: LazyGridScope.() -> Unit
) {
    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Adaptive(MusicCardImageSize),
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.innerVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(
            XyTheme.dimens.outerVerticalPadding /*VERTICAL_INTERNAL_DP.dp*/,
            Alignment.CenterHorizontally
        ),
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding,
            vertical = XyTheme.dimens.outerVerticalPadding
        ),
        content = {
            content()
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(
                    modifier = Modifier.height(
                        XyTheme.dimens.snackBarPlayerHeight + WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
                )
            }
        }
    )
}

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
    onIsLoading: () -> Boolean,
    onIfNotData: () -> Boolean,
    items: LazyPagingItems<T>?
) {

    item(span = { GridItemSpan(maxLineSpan) }) {

        val text = if (onIfNotData())
            R.string.reached_bottom
        else if (onIsLoading())
            R.string.loading
        else if (items?.loadState?.append is LoadState.Loading)
            R.string.loading
        else if (items?.loadState?.append?.endOfPaginationReached == true)
            R.string.reached_bottom
        else R.string.empty_info
        LazyLoadingAndStatus(
            text = stringResource(text),
            ifLoading = onIsLoading() || items?.loadState?.append is LoadState.Loading
        )
    }

    when {
        items?.loadState?.refresh is LoadState.Error -> {
            item(key = 7777, span = { GridItemSpan(maxLineSpan) }) {
                if (items.itemCount <= 0) {
                    //刷新的时候，如果itemCount小于0，第一次加载异常

                    ErrorContent {
                        items.retry()
                    }
                } else {
                    ErrorMoreRetryItem {
                        items.retry()
                    }
                }
            }
        }

        items?.loadState?.append is LoadState.Error -> {
            item(span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }) {
                ErrorMoreRetryItem {
                    if (retry == null)
                        items.retry()
                    else
                        retry.invoke()
                }
            }
        }
    }

}