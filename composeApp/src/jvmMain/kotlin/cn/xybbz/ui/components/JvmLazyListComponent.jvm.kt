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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.request_failed_check_network
import xymusic_kmp.composeapp.generated.resources.retry_button

/**
 * JVM 详情页使用的分页列表封装。
 *
 * 和 [SwipeRefreshListComponent] 保持相同的底部分页状态语义，但桌面端不启用下拉刷新。
 */
@Composable
internal fun JvmLazyListComponent(
    modifier: Modifier = Modifier,
    lazyState: LazyListState = rememberLazyListState(),
    pagingItems: LazyPagingItems<*>?,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    lazyColumnBottom: (LazyListScope.(PagingUiState) -> Unit)? = { pagingUiState ->
        jvmLazyColumnBottomComponent(
            pagingUiState = pagingUiState,
            retry = {
                pagingItems?.retry()
            }
        )
    },
    bottomItem: (LazyListScope.() -> Unit)? = {
        item {
            Spacer(modifier = Modifier.height(XyTheme.dimens.snackBarPlayerHeight))
        }
    },
    listContent: LazyListScope.() -> Unit,
) {
    val pagingUiState = pagingItems.toPagingUiState(treatNullAsInitialLoading = true)

    LazyColumnNotComponent(
        modifier = modifier,
        state = lazyState,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        bottomItem = bottomItem,
    ) {
        listContent()
        lazyColumnBottom?.invoke(this, pagingUiState)
    }
}

internal fun LazyListScope.jvmLazyColumnBottomComponent(
    pagingUiState: PagingUiState,
    retry: () -> Unit,
) {
    item {
        LazyLoadingAndStatus(
            text = stringResource(pagingUiState.jvmBottomText),
            ifLoading = pagingUiState.shouldShowBottomLoading,
        )
    }

    if (pagingUiState.appendError || pagingUiState.refreshError) {
        item(key = "jvm_lazy_list_retry") {
            ErrorMoreRetryItem(
                onErrorText = { Res.string.retry_button },
                retry = retry,
            )
        }
    }
}

private val PagingUiState.jvmBottomText: StringResource
    get() = when {
        refreshError || appendError -> Res.string.request_failed_check_network
        else -> bottomText
    }
