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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.theme.XyTheme

/**
 * JVM 平台列表实现。
 *
 * 普通页面仍然渲染原始 LazyColumn；当列表位于右侧弹窗内容区时，
 * 额外在列表右侧叠加桌面端垂直滚动条。
 *
 * @param modifier 列表本体的 Modifier，继续透传给 LazyColumn，避免改变原有布局尺寸。
 * @param lazyListState 列表滚动状态，同时用于驱动右侧滚动条。
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
    // 只有右侧弹窗内的列表需要桌面滚动条，避免影响普通页面已有滚动条布局。
    if (LocalModalSideSheetContent.current) {
        // Box 用来把滚动条叠在 LazyColumn 右侧，LazyColumn 本身仍保留调用方传入的 modifier。
        Box {
            LazyColumn(
                state = lazyListState,
                modifier = modifier,
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                content = content
            )

            // 当内容确实可以向上或向下滚动时才显示滚动条，短列表不占视觉空间。
            val scrollbarVisible by remember {
                derivedStateOf {
                    lazyListState.canScrollBackward || lazyListState.canScrollForward
                }
            }

            // 滚动条贴在弹窗内容区域右侧，并与当前 LazyListState 共享滚动位置。
            ModalSideSheetVerticalScrollbar(
                visible = scrollbarVisible,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                lazyListState = lazyListState
            )
        }
    } else {
        // 非右侧弹窗场景保持原始 LazyColumn，保证页面列表行为不被这次需求影响。
        LazyColumn(
            state = lazyListState,
            modifier = modifier,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

/**
 * 右侧弹窗内 LazyColumn 使用的 JVM 垂直滚动条。
 *
 * @param visible 当前列表是否可滚动；不可滚动时隐藏滚动条。
 * @param modifier 滚动条本体的布局修饰符，通常由调用方指定右侧对齐和高度。
 * @param lazyListState 与 LazyColumn 共享的滚动状态。
 */
@Composable
private fun ModalSideSheetVerticalScrollbar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState
) {
    // 使用与项目现有桌面滚动条一致的颜色、厚度和悬停动画。
    CompositionLocalProvider(
        LocalScrollbarStyle provides ScrollbarStyle(
            minimalHeight = 16.dp,
            thickness = XyTheme.dimens.outerHorizontalPadding / 2,
            shape = MaterialTheme.shapes.small,
            hoverDurationMillis = 300,
            unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
        )
    ) {
        // 滚动条出现和消失时淡入淡出，避免列表可滚动状态变化时显得突兀。
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = modifier.padding(end = XyTheme.dimens.innerHorizontalPadding / 2)
        ) {
            // rememberScrollbarAdapter 会把 LazyListState 转为桌面 VerticalScrollbar 可识别的适配器。
            VerticalScrollbar(
                modifier = Modifier.fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = lazyListState)
            )
        }
    }
}
