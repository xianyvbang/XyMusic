package cn.xybbz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.xybbz.di.initKoin
import cn.xybbz.proxy.JvmReverseProxyServer
import cn.xybbz.ui.windows.DesktopWindowDecorators
import cn.xybbz.ui.windows.DesktopWindowFrameState
import cn.xybbz.ui.windows.DesktopWindowTitleBarHitTestOwner
import cn.xybbz.ui.windows.LocalDesktopWindowChromeController
import cn.xybbz.ui.windows.LocalDesktopWindowDecorators
import cn.xybbz.ui.windows.LocalDesktopWindowFrameState
import cn.xybbz.ui.windows.rememberWindowsWindowChromeController

fun main() = application {
    initKoin {}
    // 应用启动后立即拉起本地代理服务，供封面、音频与视频流转发使用。
    JvmReverseProxyServer.start()

    val handleCloseRequest = {
        // 应用退出前主动关闭代理服务，避免残留端口占用与连接资源泄漏。
        JvmReverseProxyServer.stop()
        exitApplication()
    }
    val windowState = rememberWindowState(width = 1280.dp, height = 720.dp)

    Window(
        onCloseRequest = handleCloseRequest,
        undecorated = true,
        resizable = true,
        title = "XyMusic-KMP",
        state = windowState,

        ) {
        val chromeController = rememberWindowsWindowChromeController(
            window = window,
            onCloseRequest = handleCloseRequest
        )

        CompositionLocalProvider(
            LocalDesktopWindowChromeController provides chromeController,
            // WindowDraggableArea 只能在 desktopApp 入口侧拿到，这里向 composeApp 注入拖拽包装能力。
            LocalDesktopWindowDecorators provides object : DesktopWindowDecorators {
                @Composable
                override fun DraggableArea(
                    content: @Composable () -> Unit,
                    hitTestOwner: DesktopWindowTitleBarHitTestOwner?,
                ) {
                    content()
                }
            },
            // 窗口控制行为同样从桌面入口下发，标题栏 UI 只消费状态与回调。
            LocalDesktopWindowFrameState provides DesktopWindowFrameState(
                isMaximized = chromeController.isMaximized,
                onMinimize = {
                    chromeController.minimize()
                },
                onToggleMaximize = {
                    chromeController.toggleMaximize()
                },
                onClose = {
                    chromeController.close()
                }
            )
        ) {
            App()
        }
    }
}
