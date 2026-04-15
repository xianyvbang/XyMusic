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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.textButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyTextSubSmall
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.icon_error
import xymusic_kmp.composeapp.generated.resources.request_failed_check_network
import xymusic_kmp.composeapp.generated.resources.retry
import xymusic_kmp.composeapp.generated.resources.retry_button

/**
 * 下拉加载封装
 *
 * implementation "com.google.accompanist:accompanist-swiperefresh:xxx"
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> SwipeRefreshListComponent(
    modifier: Modifier = Modifier,
    lazyState: LazyListState = rememberLazyListState(),
    collectAsLazyPagingItems: LazyPagingItems<T>?,
    lazyColumBottom: (LazyListScope.(PagingUiState) -> Unit)? = { pagingUiState ->
        lazyColumBottomComponent(
            pagingUiState = pagingUiState,
        ) { collectAsLazyPagingItems }
    },
    bottomItem: (LazyListScope.() -> Unit)? = {
        item {
            Spacer(modifier = Modifier.height(XyTheme.dimens.snackBarPlayerHeight))
        }
    },
    listContent: LazyListScope.(LazyPagingItems<T>) -> Unit,
) {
    val pagingUiState = collectAsLazyPagingItems.toPagingUiState(treatNullAsInitialLoading = true)
    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        state = state,
        isRefreshing = pagingUiState.shouldShowPullRefreshIndicator,
        onRefresh = {
            collectAsLazyPagingItems?.refresh()
        }
    ) {
        LazyColumnNotComponent(modifier = modifier, state = lazyState, bottomItem = bottomItem) {
            collectAsLazyPagingItems?.let {
                listContent(collectAsLazyPagingItems)

            }

            lazyColumBottom?.invoke(
                this,
                pagingUiState
            )
        }
    }
}


/**
 * 列表封装
 *
 * */
@Composable
fun <T : Any> LazyListComponent(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    collectAsLazyPagingItems: LazyPagingItems<T>?,
    listContent: LazyListScope.(LazyPagingItems<T>) -> Unit,
) {
    val pagingUiState = collectAsLazyPagingItems.toPagingUiState(treatNullAsInitialLoading = true)

    LazyColumnNotComponent(modifier = modifier, state = state) {
        collectAsLazyPagingItems?.let {
            listContent(collectAsLazyPagingItems)
        }
        lazyColumBottomComponent(
            pagingUiState = pagingUiState,
        ) { collectAsLazyPagingItems }
    }
}

/**
 * lazyColum底部组件集合
 */
fun <T : Any> LazyListScope.lazyColumBottomComponent(
    retry: (() -> Unit)? = null,
    pagingUiState: PagingUiState,
    onHomeMusicPagingItems: () -> LazyPagingItems<T>?
) {

    item {
        LazyLoadingAndStatus(
            text = stringResource(pagingUiState.bottomText),
            ifLoading = pagingUiState.shouldShowBottomLoading
        )
    }

    when {
        pagingUiState.appendError -> {
            //加载更多异常
            item(key = 8888) {
                ErrorMoreRetryItem {
                    if (retry == null)
                        onHomeMusicPagingItems()?.retry()
                    else
                        retry.invoke()
                }
            }

        }

        pagingUiState.refreshError -> {
            item(key = 7777) {
                if (!pagingUiState.hasData) {
                    //刷新的时候，如果itemCount小于0，第一次加载异常

                    ErrorContent {
                        onHomeMusicPagingItems()?.retry()
                    }
                } else {
                    ErrorMoreRetryItem {
                        onHomeMusicPagingItems()?.retry()
                    }
                }
            }
        }
    }
}

/**
 * 底部加载更多失败处理
 * */
@Composable
fun ErrorMoreRetryItem(
    onErrorText: () -> StringResource = { Res.string.retry },
    retry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextButton(
            onClick = { retry() },
            modifier = Modifier
                .padding(20.dp)
//                .width(80.dp)
                .height(30.dp),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(3.dp),
            colors = textButtonColors(containerColor = Color.Gray),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp,
            ),
        ) {
            Text(text = stringResource(onErrorText()), color = Color.White)
        }
    }
}

/**
 * 页面加载失败处理
 * */
@Composable
fun ErrorContent(retry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.padding(top = 80.dp),
            painter = painterResource(Res.drawable.icon_error),
            contentDescription = null
        )
        Text(
            text = stringResource(Res.string.request_failed_check_network),
            modifier = Modifier.padding(8.dp)
        )
        TextButton(
            onClick = { retry() },
            modifier = Modifier
                .padding(20.dp)
                .width(80.dp)
                .height(30.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(5.dp),
            colors = textButtonColors(containerColor = Color.Gray),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp,
            )
        ) { Text(text = stringResource(Res.string.retry_button), color = Color.White) }
    }
}

@Composable
fun LazyLoadingAndStatus(text: String, ifLoading: Boolean) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),
        contentAlignment = Alignment.Center
    ) {
        val lineWidth = this.maxWidth / 4
        LazyLoadingAndStatusInfo(text = text, lineWidth = lineWidth, ifLoading = ifLoading)
    }
}

@Composable
fun LazyLoadingAndStatusInfo(text: String, lineWidth: Dp, ifLoading: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(modifier = Modifier.width(lineWidth))
        Spacer(modifier = Modifier.width(XyTheme.dimens.corner))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (ifLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp),
                    color = Color.Gray,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(XyTheme.dimens.corner))
            }
            XyTextSubSmall(text = text)
        }
        Spacer(modifier = Modifier.width(XyTheme.dimens.corner))
        HorizontalDivider(modifier = Modifier.width(lineWidth))
    }
}
