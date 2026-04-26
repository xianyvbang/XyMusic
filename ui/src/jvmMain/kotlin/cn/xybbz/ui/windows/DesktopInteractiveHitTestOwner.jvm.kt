package cn.xybbz.ui.windows

import androidx.compose.ui.geometry.Rect

/**
 * 标题栏交互热区记录器。
 * 它只关心“哪些矩形区域属于 Compose 控件”，供原生窗口命中测试排除这些区域，
 * 从而保留按钮点击、输入框聚焦等交互，不把它们误判成窗口拖拽。
 */
class DesktopInteractiveHitTestOwner : DesktopWindowTitleBarHitTestOwner {
    private val interactiveBounds = mutableMapOf<String, Rect>()

    /**
     * 判断当前坐标是否命中任意一个已登记的交互区域。
     * 返回 `true` 表示这里应该交给 Compose 控件处理，而不是当成标题栏空白拖拽区。
     */
    override fun hitTest(x: Float, y: Float): Boolean {
        return interactiveBounds.values.any { bounds ->
            x >= bounds.left && x < bounds.right && y >= bounds.top && y < bounds.bottom
        }
    }

    /**
     * 记录指定交互控件当前在窗口中的位置。
     * 组件发生重组或布局变化时，会持续更新这些边界，保证命中测试结果准确。
     */
    fun updateBounds(targetId: String, bounds: Rect) {
        interactiveBounds[targetId] = bounds
    }

    fun removeBounds(targetId: String) {
        interactiveBounds.remove(targetId)
    }

    fun clear() {
        interactiveBounds.clear()
    }
}
