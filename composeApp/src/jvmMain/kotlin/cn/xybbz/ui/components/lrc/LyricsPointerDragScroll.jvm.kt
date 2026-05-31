package cn.xybbz.ui.components.lrc

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.lyricsPointerDragScroll(
    listState: LazyListState,
    onDragStateChanged: (Boolean) -> Unit
): Modifier = pointerInput(listState) {
    detectVerticalDragGestures(
        onDragStart = {
            onDragStateChanged(true)
        },
        onVerticalDrag = { change, dragAmount ->
            change.consume()
            listState.dispatchRawDelta(-dragAmount)
        },
        onDragEnd = {
            onDragStateChanged(false)
        },
        onDragCancel = {
            onDragStateChanged(false)
        }
    )
}
