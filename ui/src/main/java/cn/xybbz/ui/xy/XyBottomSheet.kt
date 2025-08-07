package cn.xybbz.ui.xy

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit
) {
    if (onIfDisplay())
        ModalBottomSheet(
            modifier = modifier,
            sheetState = bottomSheetState,
            containerColor = containerColor,
            contentWindowInsets = contentWindowInsets,
            dragHandle = dragHandle,
            onDismissRequest = {
                onClose(false)
            }, content = content
        )
}