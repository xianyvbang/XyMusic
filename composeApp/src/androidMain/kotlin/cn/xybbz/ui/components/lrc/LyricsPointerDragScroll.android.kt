package cn.xybbz.ui.components.lrc

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier

actual fun Modifier.lyricsPointerDragScroll(
    listState: LazyListState,
    onDragStateChanged: (Boolean) -> Unit
): Modifier = this
