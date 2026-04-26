package cn.xybbz.ui.windows

/**
 * 桌面窗口状态与控制动作。
 * 由 desktopApp 注入，供 JVM 标题栏读取最小化、最大化和关闭能力。
 */
data class DesktopWindowFrameState(
    val isMaximized: Boolean,
    val onMinimize: () -> Unit,
    val onToggleMaximize: () -> Unit,
    val onClose: () -> Unit,
) {
    companion object {
        val None = DesktopWindowFrameState(
            isMaximized = false,
            onMinimize = {},
            onToggleMaximize = {},
            onClose = {},
        )
    }
}
