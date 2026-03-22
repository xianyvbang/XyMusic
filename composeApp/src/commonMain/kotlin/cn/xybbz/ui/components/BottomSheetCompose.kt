package cn.xybbz.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cn.xybbz.ui.xy.ModalBottomSheetExtendComponent
import kotlinx.coroutines.launch


val bottomSheetObjectList = mutableStateListOf<BottomSheetObject>()

@OptIn(ExperimentalMaterial3Api::class)
data class BottomSheetObject(
    val sheetState: SheetState,
    val state: Boolean = true,
    val titleText: String? = null,
    val titleTailContent: (@Composable RowScope.() -> Unit)? = null,
    val onClose: (suspend (Boolean) -> Unit)? = null,
    val dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    val content: @Composable ColumnScope.(BottomSheetObject) -> Unit,
    val containerColor: @Composable () -> Color = { MaterialTheme.colorScheme.background },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetCompose(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    bottomSheetObjectList.forEach { bottomSheet ->
        ModalBottomSheetExtendComponent(
            modifier = modifier.statusBarsPadding(),
            containerColor = bottomSheet.containerColor(),
            bottomSheetState = bottomSheet.sheetState,
            onIfDisplay = { bottomSheet.state },
            onClose = { bool ->
                coroutineScope.launch {
                    bottomSheet.onClose?.invoke(bool)
                    bottomSheet.sheetState.hide()
                }.invokeOnCompletion {
                    bottomSheet.dismiss()
                }
            },
            titleText = bottomSheet.titleText,
            titleTailContent = bottomSheet.titleTailContent,
            dragHandle = bottomSheet.dragHandle,
            content = {
                bottomSheet.content(this,bottomSheet)
            }
        )
    }
}

/**
 * 关闭弹窗
 */
fun BottomSheetObject.dismiss() = apply {
    mainMoeScope.launch {
        bottomSheetObjectList.remove(this@dismiss)
    }
}

/**
 * 显示弹窗
 */
fun BottomSheetObject.show() = apply {
    mainMoeScope.launch {
        bottomSheetObjectList.add(this@show)
    }
}
