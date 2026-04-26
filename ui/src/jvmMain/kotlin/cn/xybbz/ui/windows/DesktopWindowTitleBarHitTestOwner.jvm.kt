package cn.xybbz.ui.windows

/**
 * 标题栏命中检测能力。
 * 用于区分标题栏空白区与 Compose 交互控件，避免误触发窗口拖拽。
 */
interface DesktopWindowTitleBarHitTestOwner {
    fun hitTest(x: Float, y: Float): Boolean
}
