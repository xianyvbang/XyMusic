package cn.xybbz.config.window

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.geometry.Rect

/**
 * 桌面端原生窗口标题栏桥接能力。
 * Windows 通过它同步标题栏热区并触发原生最小化、最大化动画。
 */
interface DesktopWindowChromeController {
    val isMaximized: Boolean
    val windowInsets: WindowInsets

    fun updateTitleBarBounds(bounds: Rect)

    fun updateMinimizeButtonBounds(bounds: Rect)

    fun updateMaximizeButtonBounds(bounds: Rect)

    fun updateCloseButtonBounds(bounds: Rect)

    fun setTitleBarHitTestOwner(hitTestOwner: DesktopWindowTitleBarHitTestOwner?)

    fun minimize()

    fun toggleMaximize()

    companion object {
        val None: DesktopWindowChromeController = object : DesktopWindowChromeController {
            private val zeroInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0)

            override val isMaximized: Boolean = false
            override val windowInsets: WindowInsets = zeroInsets

            override fun updateTitleBarBounds(bounds: Rect) = Unit

            override fun updateMinimizeButtonBounds(bounds: Rect) = Unit

            override fun updateMaximizeButtonBounds(bounds: Rect) = Unit

            override fun updateCloseButtonBounds(bounds: Rect) = Unit

            override fun setTitleBarHitTestOwner(hitTestOwner: DesktopWindowTitleBarHitTestOwner?) = Unit

            override fun minimize() = Unit

            override fun toggleMaximize() = Unit
        }
    }
}
