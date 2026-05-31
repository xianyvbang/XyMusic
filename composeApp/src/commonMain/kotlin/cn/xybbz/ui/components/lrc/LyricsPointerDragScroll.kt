package cn.xybbz.ui.components.lrc

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier

expect fun Modifier.lyricsPointerDragScroll(
    listState: LazyListState,
    onDragStateChanged: (Boolean) -> Unit
): Modifier
