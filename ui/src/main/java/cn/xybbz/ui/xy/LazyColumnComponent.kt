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

package cn.xybbz.ui.xy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.theme.XyTheme

/**
 * 通用LazyColumn
 * @param [modifier] 样式
 * @param [items] Lazy的item
 */
@Composable
fun LazyColumnComponent(
    modifier: Modifier = Modifier,
    items: LazyListScope.() -> Unit
) {
    LazyColumnParentComponent(
        modifier = modifier
            .fillMaxWidth(),
        content = {
            items()
            item {
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

@Composable
fun LazyColumnNotComponent(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    bottomItem: (LazyListScope.() -> Unit)? = {
        item {
            Spacer(
                modifier = Modifier.height(
                    XyTheme.dimens.snackBarPlayerHeight + WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                )
            )
        }
    },
    items: LazyListScope.() -> Unit
) {
    LazyColumnParentComponent(
        modifier = modifier
            .fillMaxWidth(),
        lazyListState = state,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = {
            items()
            bottomItem?.invoke(this)
        }
    )
}

/**
 * 通用LazyColumn
 * @param [modifier] 样式
 */
@Composable
fun LazyColumnParentComponent(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(
        vertical = XyTheme.dimens.outerVerticalPadding,
        horizontal = XyTheme.dimens.outerHorizontalPadding
    ),
    verticalArrangement: Arrangement.Vertical =
        Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun LazyColumnBottomSheetComponent(
    modifier: Modifier = Modifier,
    horizontal: Dp = 0.dp,
    vertical: Dp = XyTheme.dimens.outerVerticalPadding,
    content: LazyListScope.() -> Unit
) {
    LazyColumnParentComponent(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(
            horizontal = horizontal,
            vertical = vertical
        ),
        content = content
    )
}