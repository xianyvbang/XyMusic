package cn.xybbz.compositionLocal

import androidx.compose.runtime.compositionLocalOf
import cn.xybbz.common.constants.Constants
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
