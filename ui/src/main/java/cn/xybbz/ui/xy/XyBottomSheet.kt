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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetExtendComponent(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentWindowInsets: @Composable () -> WindowInsets = {
        WindowInsets.Companion.systemBars.only(
            WindowInsetsSides.Top
        )
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
    if (onIfDisplay())
        ModalBottomSheet(
            modifier = modifier,
            sheetState = bottomSheetState,
            containerColor = containerColor,
            contentWindowInsets = contentWindowInsets,
            dragHandle = null,
            onDismissRequest = {
                onClose(false)
            }, content = {
                XyColumn(
                    modifier = Modifier
                        .brashColor()
                        .navigationBarsPadding(),
                    backgroundColor = Color.Transparent,
                    paddingValues = PaddingValues(
                        vertical = XyTheme.dimens.outerVerticalPadding
                    )
                ) {
                    dragHandle?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            dragHandle()
                        }
                    }
                    titleText?.let {
                        XyRow(
                            paddingValues = PaddingValues(
                                top = XyTheme.dimens.innerVerticalPadding,
                                start = XyTheme.dimens.innerHorizontalPadding,
                                end = XyTheme.dimens.innerHorizontalPadding,
                                bottom = XyTheme.dimens.innerVerticalPadding
                            )
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.Start
                            ) {
                                XyText(
                                    text = titleText,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Spacer(modifier = Modifier.width(XyTheme.dimens.innerHorizontalPadding))
                                titleSub?.let {
                                    XyTextSub(
                                        text = titleSub,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            titleTailContent?.invoke(this)
                        }
                    }
                    content()
                }
            }
        )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetExtendFillMaxSizeComponent(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentWindowInsets: @Composable () -> WindowInsets = {
        WindowInsets.Companion.systemBars.only(
            WindowInsetsSides.Top
        )
    },
    bottomSheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    ),
    onIfDisplay: () -> Boolean,
    onClose: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onIfDisplay())
        ModalBottomSheet(
            modifier = modifier,
            sheetState = bottomSheetState,
            containerColor = containerColor,
            contentWindowInsets = contentWindowInsets,
            dragHandle = null,
            onDismissRequest = {
                onClose(false)
            }, content = content
        )
}