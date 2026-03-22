package cn.xybbz.ui.components.lrc

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import kotlin.math.hypot

@SuppressLint("ReturnFromAwaitPointerEventScope", "MultipleAwaitPointerEventScopes")
fun Modifier.detectDragState(
    onDragStateChanged: (Boolean) -> Unit
): Modifier = composed {
    val density = LocalDensity.current
    val viewConfiguration = LocalViewConfiguration.current
    val touchSlopPx = with(density) { viewConfiguration.touchSlop }

    var isDragging by remember { mutableStateOf(false) }
    var startX by remember { mutableFloatStateOf(0f) }
    var startY by remember { mutableFloatStateOf(0f) }

    this.pointerInput(Unit) {
        while (true) {
            // 只在 Initial 阶段观察，不消费事件
            val down = awaitPointerEventScope { awaitPointerEvent(PointerEventPass.Initial) }
            val downChange = down.changes.firstOrNull()
            if (downChange == null || !downChange.pressed) continue

            startX = downChange.position.x
            startY = downChange.position.y

            awaitPointerEventScope {
                while (true) {
                    val move = awaitPointerEvent(PointerEventPass.Initial)
                    val moveChange = move.changes.firstOrNull { it.id == downChange.id } ?: break

                    val dx = moveChange.position.x - startX
                    val dy = moveChange.position.y - startY
                    val distance = hypot(dx, dy)

                    if (distance > touchSlopPx && !isDragging) {
                        isDragging = true
                        onDragStateChanged(true)
                    }

                    if (!moveChange.pressed) {
                        if (isDragging) {
                            isDragging = false
                            onDragStateChanged(false)
                        }
                        break
                    }
                }
            }
        }
    }
}

