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

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems

@Composable
internal fun <T : Any> JvmVerticalGridListComponent(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    collectAsLazyPagingItems: LazyPagingItems<T>,
    content: LazyGridScope.() -> Unit,
) {
    val pagingUiState = collectAsLazyPagingItems.toPagingUiState()
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val scrollbarVisible by remember {
        derivedStateOf {
            (hovered || lazyGridState.isScrollInProgress) &&
                (lazyGridState.canScrollBackward || lazyGridState.canScrollForward)
        }
    }

    Box(
        modifier = modifier.hoverable(interactionSource = interactionSource),
    ) {
        LazyVerticalGridComponent(
            modifier = Modifier.fillMaxSize(),
            pageListItems = collectAsLazyPagingItems,
            lazyGridState = lazyGridState,
            pagingUiState = pagingUiState,
            content = content,
        )

        SidebarVerticalScrollbar(
            visible = scrollbarVisible,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState = lazyGridState),
        )
    }
}
