package cn.xybbz.compositionLocal

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import cn.xybbz.common.constants.Constants
import cn.xybbz.config.window.DesktopWindowChromeController
import cn.xybbz.config.window.DesktopWindowDecorators
import cn.xybbz.entity.data.DesktopWindowFrameState

/**
 * 桌面窗口控制状态。
 * 由 desktopApp 在窗口入口处注入，供 JVM 标题栏读取最小化、最大化和关闭能力。
 */
val LocalDesktopWindowFrameState =
    compositionLocalOf<DesktopWindowFrameState> { error(Constants.COMPOSITION_LOCAL_ERROR) }

/**
 * 桌面窗口装饰能力。
 * 目前主要用于向 composeApp 注入可拖拽区域包装器，避免直接依赖 desktopApp 的 Window API。
 */
val LocalDesktopWindowDecorators =
    compositionLocalOf<DesktopWindowDecorators> { error(Constants.COMPOSITION_LOCAL_ERROR) }

/**
 * 桌面窗口原生标题栏桥接能力。
 * Windows 端通过它同步标题栏热区与系统窗口行为，其它平台使用空实现。
 */
val LocalDesktopWindowChromeController =
    compositionLocalOf<DesktopWindowChromeController> { DesktopWindowChromeController.None }

/**
 * 桌面标题栏交互热区登记器。
 * 标题栏子组件通过它把按钮、输入框等可交互区域上报给窗口命中检测层，
 * 这样系统只会在真正的留白区触发拖动窗口，不会吞掉控件点击。
 */
internal val LocalDesktopTitleBarHitTestOwner =
    staticCompositionLocalOf<DesktopInteractiveHitTestOwner> {
        error("DesktopInteractiveHitTestOwner not provided")
    }
