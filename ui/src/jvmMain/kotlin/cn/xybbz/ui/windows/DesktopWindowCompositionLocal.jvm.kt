package cn.xybbz.ui.windows

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 桌面窗口控制状态。
 * 由 desktopApp 在窗口入口处注入，供 JVM 标题栏读取最小化、最大化和关闭能力。
 */
val LocalDesktopWindowFrameState =
    compositionLocalOf { DesktopWindowFrameState.None }

/**
 * 桌面窗口装饰能力。
 * 目前主要用于向 composeApp 注入可拖拽区域包装器，避免直接依赖 desktopApp 的 Window API。
 */
val LocalDesktopWindowDecorators =
    compositionLocalOf<DesktopWindowDecorators> { error("DesktopWindowDecorators not provided") }

/**
 * 桌面原生窗口控制器。
 * Windows 入口会提供真实实现，非 Windows 或预览场景使用 no-op 兜底。
 */
val LocalDesktopWindowChromeController =
    compositionLocalOf<DesktopWindowChromeController> { DesktopWindowChromeController.None }

/**
 * 桌面标题栏交互热区登记器。
 * 标题栏子组件通过它把按钮、输入框等可交互区域上报给窗口命中检测层，
 * 这样系统只会在真正的留白区触发拖动窗口，不会吞掉控件点击。
 */
val LocalDesktopTitleBarHitTestOwner =
    staticCompositionLocalOf<DesktopInteractiveHitTestOwner> {
        error("DesktopInteractiveHitTestOwner not provided")
    }
