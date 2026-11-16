package cn.xybbz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.xybbz.config.setting.JvmAutoStartManager
import cn.xybbz.config.setting.SettingsManager
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
import java.awt.TrayIcon
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener
import javax.swing.JFrame

/**
 * JVM 桌面端应用入口。
 */
fun main() {
    // FileKit 需要在桌面应用启动前初始化 appId，用于解析平台目录和原生文件弹窗能力。
    FileKit.init(appId = "cn.xybbz")
    // Koin 只注册依赖，真正的启动初始化在 App/StartupViewModel 内异步执行。
    val koinApplication = initKoin {}
    // 窗口关闭行为需要读取桌面端启动设置，这里直接从 Koin 取设置管理器。
    val settingsManager = koinApplication.koin.get<SettingsManager>()

    // 防止多个窗口关闭入口重复释放同一批应用级资源。
    var isClosing = false
    // 主窗口与托盘图标需要在关闭回调与托盘菜单之间共享。
    var mainWindow: ComposeWindow? = null
    var trayIcon: TrayIcon? = null

    application {
        // 真正退出应用：停止代理服务、释放 Koin 单例并结束桌面事件循环。
        val exitApplicationSafely: () -> Unit = {
            if (!isClosing) {
                isClosing = true
                DesktopTray.dispose(trayIcon)
                trayIcon = null
                try {
                    JvmReverseProxyServer.stop()
                } finally {
                    try {
                        koinApplication.close()
                    } finally {
                        exitApplication()
                    }
                }
            }
        }

        // 从托盘恢复主窗口，并处理最小化状态。
        val showMainWindow: () -> Unit = {
            mainWindow?.let { window ->
                window.isVisible = true
                if ((window.extendedState and JFrame.ICONIFIED) != 0) {
                    window.extendedState = JFrame.NORMAL
                }
                window.toFront()
                window.requestFocus()
            }
        }

        // 关闭窗口：开启“最小化到托盘”时隐藏窗口，否则直接退出应用。
        val requestCloseWindow: () -> Unit = {
            if (!isClosing) {
                val window = mainWindow
                val minimizeToTray = settingsManager.get().ifMinimizeToTray
                if (window != null && minimizeToTray && DesktopTray.isSupported()) {
                    // 托盘图标按需安装，避免未开启该设置时常驻托盘。
                    if (trayIcon == null) {
                        trayIcon = DesktopTray.install(showMainWindow, exitApplicationSafely)
                    }
                    if (trayIcon != null) {
                        window.isVisible = false
                    } else {
                        // 托盘安装失败时回退为正常退出，避免窗口隐藏后无法找回。
                        exitApplicationSafely()
                    }
                } else {
                    exitApplicationSafely()
                }
            }
        }

        val windowState = rememberWindowState(width = 1280.dp, height = 720.dp)

        Window(
            onCloseRequest = requestCloseWindow,
            undecorated = true,
            resizable = true,
            title = "XyMusic",
            state = windowState,
//            alwaysOnTop = true,
        ) {
            // 设置页关闭“最小化到托盘”后，及时移除已经安装的托盘图标。
            val minimizeToTray by settingsManager.ifMinimizeToTray.collectAsState()
            LaunchedEffect(minimizeToTray) {
                if (!minimizeToTray) {
                    DesktopTray.dispose(trayIcon)
                    trayIcon = null
                }
            }

            DisposableEffect(window) {
                mainWindow = window
                onDispose {
                    if (mainWindow === window) {
                        mainWindow = null
                    }
                    DesktopTray.dispose(trayIcon)
                    trayIcon = null
                }
            }

            val chromeController = rememberWindowsWindowChromeController(
                window = window,
                onCloseRequest = requestCloseWindow
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
                    // 已开启开机自启时重新写入启动项，保证应用更新或移动后启动路径仍然有效。
                    if (settingsManager.get().ifAutoStart) {
                        JvmAutoStartManager.setEnabled(true)
                    }
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
