package cn.xybbz.ui.xy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetExtendComponent(
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
                XyColumnNotHorizontalPadding(
                    modifier = Modifier
                        .brashColor()
                        .navigationBarsPadding(),
                    backgroundColor = Color.Transparent
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
                                start = XyTheme.dimens.outerHorizontalPadding,
                                end = XyTheme.dimens.outerHorizontalPadding,
                                bottom = XyTheme.dimens.outerVerticalPadding
                            )
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.Start
                            ) {
                                XyItemTitle(text = titleText, fontSize = 18.sp)
                                titleSub?.let {
                                    Text(
                                        text = titleSub,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.titleSmall
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