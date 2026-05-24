package cn.xybbz.ui.windows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import java.util.concurrent.atomic.AtomicInteger

private val desktopWindowDragAreaTargetIdSeed = AtomicInteger()

@Composable
fun rememberDesktopWindowDragAreaTargetId(prefix: String): String {
    return remember(prefix) {
        "$prefix-${desktopWindowDragAreaTargetIdSeed.incrementAndGet()}"
    }
}

@Composable
fun Modifier.desktopWindowDragArea(
    targetId: String = rememberDesktopWindowDragAreaTargetId("DesktopWindowDragArea"),
    chromeController: DesktopWindowChromeController = LocalDesktopWindowChromeController.current,
): Modifier {
    DisposableEffect(chromeController, targetId) {
        onDispose {
            chromeController.removeDraggableAreaBounds(targetId)
        }
    }

    return onGloballyPositioned { coordinates ->
        chromeController.updateDraggableAreaBounds(targetId, coordinates.boundsInWindow())
    }
}
