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

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MusicBottomMenuPlatformSheet(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentWindowInsets: @Composable () -> WindowInsets = {
        WindowInsets.systemBars.only(WindowInsetsSides.Top)
    },
    bottomSheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    ),
    onIfDisplay: () -> Boolean,
    onClose: (Boolean) -> Unit,
    titleText: String? = null,
    titleSub: String? = null,
    titleTailContent: (@Composable RowScope.() -> Unit)? = null,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit
) {
    PlatformMusicBottomMenuSheet(
        modifier = modifier,
        containerColor = containerColor,
        contentWindowInsets = contentWindowInsets,
        bottomSheetState = bottomSheetState,
        onIfDisplay = onIfDisplay,
        onClose = onClose,
        titleText = titleText,
        titleSub = titleSub,
        titleTailContent = titleTailContent,
        dragHandle = dragHandle,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal expect fun PlatformMusicBottomMenuSheet(
    modifier: Modifier,
    containerColor: Color,
    contentWindowInsets: @Composable () -> WindowInsets,
    bottomSheetState: SheetState,
    onIfDisplay: () -> Boolean,
    onClose: (Boolean) -> Unit,
    titleText: String?,
    titleSub: String?,
    titleTailContent: (@Composable RowScope.() -> Unit)?,
    dragHandle: @Composable (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
)
