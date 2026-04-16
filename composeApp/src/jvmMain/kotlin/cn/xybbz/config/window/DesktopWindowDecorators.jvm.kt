package cn.xybbz.config.window

import androidx.compose.runtime.Composable

/**
 * 桌面入口提供的窗口装饰能力约定。
 * 目前只暴露可拖拽区域包装器，供 composeApp 的标题栏复用。
 */
interface DesktopWindowDecorators {
    @Composable
    fun DraggableArea(content: @Composable () -> Unit)
}
