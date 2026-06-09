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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.xy.ModalSideSheetExtendComponent

private const val MusicBottomMenuSideSheetAnimationMillis = 220

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun PlatformMusicBottomMenuSheet(
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
) {
    ModalSideSheetExtendComponent(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        sheetWidth = 420.dp,
        sheetMaxWidth = 520.dp,
        containerColor = containerColor,
        animationDurationMillis = MusicBottomMenuSideSheetAnimationMillis,
        useDialog = true,
        onIfDisplay = onIfDisplay,
        onClose = onClose,
        titleText = titleText,
        titleSub = titleSub,
        titleTailContent = titleTailContent,
        content = content
    )
}
