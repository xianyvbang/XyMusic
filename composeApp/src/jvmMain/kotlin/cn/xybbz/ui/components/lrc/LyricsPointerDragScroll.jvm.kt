package cn.xybbz.ui.components.lrc

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val LyricsWheelScrollStateTimeoutMillis = 500L

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
}.pointerInput(Unit) {
    coroutineScope {
        val scrollScope = this
        var scrollEndJob: Job? = null

        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val hasVerticalScroll = event.type == PointerEventType.Scroll &&
                    event.changes.any { change -> change.scrollDelta.y != 0f }
                if (!hasVerticalScroll) continue

                onDragStateChanged(true)
                scrollEndJob?.cancel()
                scrollEndJob = scrollScope.launch {
                    delay(LyricsWheelScrollStateTimeoutMillis)
                    onDragStateChanged(false)
                }
            }
        }
    }
}
