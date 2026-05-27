package cn.xybbz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import cn.xybbz.ui.xy.LocalModalSideSheetContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun main() = application {
    // Koin 只注册依赖，真正的启动初始化在 App/StartupViewModel 内异步执行。
    initKoin {}

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

        LaunchedEffect(Unit) {
            // 窗口首帧先渲染，再后台拉起本地代理服务，避免代理绑定端口阻塞桌面窗口显示。
            withContext(Dispatchers.IO) {
                JvmReverseProxyServer.start()
            }
        }

        CompositionLocalProvider(
            // JVM 桌面端统一给通用 LazyColumn 显示右侧滚动条。
            LocalModalSideSheetContent provides true,
            LocalDesktopWindowChromeController provides chromeController,
            // WindowDraggableArea 只能在 desktopApp 入口侧拿到，这里向 composeApp 注入拖拽包装能力。
            LocalDesktopWindowDecorators provides object : DesktopWindowDecorators {
                @Composable
                //todo 这里的东西没有生效
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
