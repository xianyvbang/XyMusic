package cn.xybbz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.xybbz.di.initKoin
import cn.xybbz.proxy.JvmReverseProxyServer
import cn.xybbz.ui.windows.DesktopWindowDecorators
import cn.xybbz.ui.windows.DesktopWindowFrameState
import cn.xybbz.ui.windows.DesktopWindowTitleBarHitTestOwner
import cn.xybbz.ui.windows.LocalDesktopParentWindow
import cn.xybbz.ui.windows.LocalDesktopWindowChromeController
import cn.xybbz.ui.windows.LocalDesktopWindowDecorators
import cn.xybbz.ui.windows.LocalDesktopWindowFrameState
import cn.xybbz.ui.windows.rememberWindowsWindowChromeController
import cn.xybbz.ui.xy.LocalModalSideSheetContent
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

/**
 * JVM 桌面端应用入口。
 */
fun main() {
    // FileKit 需要在桌面应用启动前初始化 appId，用于解析平台目录和原生文件弹窗能力。
    FileKit.init(appId = "cn.xybbz")

    application {
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
            title = "XyMusic",
            state = windowState,
//            alwaysOnTop = true,
        ) {
            val chromeController = rememberWindowsWindowChromeController(
                window = window,
                onCloseRequest = handleCloseRequest
            )
            var isWindowFocused by remember(window) {
                mutableStateOf(window.isFocused)
            }

            DisposableEffect(window) {
                val focusListener = object : WindowFocusListener {
                    override fun windowGainedFocus(event: WindowEvent) {
                        isWindowFocused = true
                    }

                    override fun windowLostFocus(event: WindowEvent) {
                        isWindowFocused = false
                    }
                }
                window.addWindowFocusListener(focusListener)
                isWindowFocused = window.isFocused
                onDispose {
                    window.removeWindowFocusListener(focusListener)
                }
            }

            LaunchedEffect(Unit) {
                // 窗口首帧先渲染，再后台拉起本地代理服务，避免代理绑定端口阻塞桌面窗口显示。
                withContext(Dispatchers.IO) {
                    JvmReverseProxyServer.start()
                }
            }

            CompositionLocalProvider(
                // JVM 桌面端统一给通用 LazyColumn 显示右侧滚动条。
                LocalModalSideSheetContent provides true,
                // 原生文件/目录选择器需要 parent window，避免弹窗落在主窗口后面。
                LocalDesktopParentWindow provides window,
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
                    isFocused = isWindowFocused,
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
}
