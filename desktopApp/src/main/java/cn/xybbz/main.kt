package cn.xybbz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.xybbz.compositionLocal.LocalDesktopWindowChromeController
import cn.xybbz.compositionLocal.LocalDesktopWindowDecorators
import cn.xybbz.compositionLocal.LocalDesktopWindowFrameState
import cn.xybbz.config.window.DesktopWindowChromeController
import cn.xybbz.config.window.DesktopWindowDecorators
import cn.xybbz.di.initKoin
import cn.xybbz.entity.data.DesktopWindowFrameState
import cn.xybbz.proxy.JvmReverseProxyServer
import cn.xybbz.window.WindowsDesktopWindowChromeController
import org.jetbrains.skiko.hostOs

fun main() = application {
    initKoin {}
    // 应用启动后立即拉起本地代理服务，供封面、音频与视频流转发使用。
    JvmReverseProxyServer.start()

    val handleCloseRequest = {
        // 应用退出前主动关闭代理服务，避免残留端口占用与连接资源泄漏。
        JvmReverseProxyServer.stop()
        exitApplication()
    }
    val windowState = rememberWindowState()
    val isWindows = hostOs.isWindows

    Window(
        onCloseRequest = handleCloseRequest,
        undecorated = !isWindows,
        resizable = true,
        title = "XyMusic-KMP",
        state = windowState,
    ) {
        val chromeController = remember(window, isWindows) {
            if (isWindows) {
                WindowsDesktopWindowChromeController(window)
            } else {
                DesktopWindowChromeController.None
            }
        }
        CompositionLocalProvider(
            LocalDesktopWindowChromeController provides chromeController,
            // WindowDraggableArea 只能在 desktopApp 入口侧拿到，这里向 composeApp 注入拖拽包装能力。
            LocalDesktopWindowDecorators provides object : DesktopWindowDecorators {
                @Composable
                override fun DraggableArea(content: @Composable () -> Unit) {
                    WindowDraggableArea {
                        content()
                    }
                }
            },
            // 窗口控制行为同样从桌面入口下发，标题栏 UI 只消费状态与回调。
            LocalDesktopWindowFrameState provides DesktopWindowFrameState(
                isMaximized = if (isWindows) chromeController.isMaximized else windowState.placement == WindowPlacement.Maximized,
                onMinimize = {
                    if (isWindows) {
                        chromeController.minimize()
                    } else {
                        windowState.isMinimized = true
                    }
                },
                onToggleMaximize = {
                    if (isWindows) {
                        chromeController.toggleMaximize()
                    } else {
                        windowState.placement =
                            if (windowState.placement == WindowPlacement.Maximized) {
                                WindowPlacement.Floating
                            } else {
                                WindowPlacement.Maximized
                            }
                    }
                },
                onClose = handleCloseRequest
            )
        ) {
            Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
            ) {
                App()
            }
        }
    }
}
