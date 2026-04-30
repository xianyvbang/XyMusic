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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * iOS 平台列表实现。
 *
 * iOS 端继续保留原始 LazyColumn，不额外显示桌面端滚动条。
 *
 * @param modifier 列表本体的 Modifier。
 * @param lazyListState 列表滚动状态。
 * @param contentPadding 列表内容内边距。
 * @param verticalArrangement 列表项的垂直排列方式。
 * @param horizontalAlignment 列表项的水平对齐方式。
 * @param content LazyColumn 的列表内容。
 */
@Composable
internal actual fun PlatformLazyColumn(
    modifier: Modifier,
    lazyListState: LazyListState,
    contentPadding: PaddingValues,
    verticalArrangement: Arrangement.Vertical,
    horizontalAlignment: Alignment.Horizontal,
    content: LazyListScope.() -> Unit
) {
    // iOS 保持原始 LazyColumn 行为，避免改变移动端 bottomSheet 视觉和手势体验。
    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}
