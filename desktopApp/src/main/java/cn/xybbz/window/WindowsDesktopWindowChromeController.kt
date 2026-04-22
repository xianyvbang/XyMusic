package cn.xybbz.window

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Rect
import cn.xybbz.config.window.DesktopWindowChromeController
import cn.xybbz.config.window.DesktopWindowTitleBarHitTestOwner
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.POINT
import com.sun.jna.platform.win32.WinDef.RECT
import com.sun.jna.platform.win32.WinDef.WPARAM
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.platform.win32.WinUser.WM_DESTROY
import com.sun.jna.platform.win32.WinUser.WM_SIZE
import com.sun.jna.ptr.IntByReference
import org.jetbrains.skiko.SkiaLayer
import java.awt.Container
import java.awt.Window
import javax.swing.JComponent

internal typealias WindowProcedure = WinUser.WindowProc

class WindowsDesktopWindowChromeController(
    private val window: Window,
) : DesktopWindowChromeController {
    private val zeroInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0)

    private var titleBarBounds = Rect.Zero
    private var minimizeButtonBounds = Rect.Zero
    private var maximizeButtonBounds = Rect.Zero
    private var closeButtonBounds = Rect.Zero
    private var titleBarHitTestOwner: DesktopWindowTitleBarHitTestOwner? = null
    private var titleBarHitTestEnabled = true

    private var insetsState by mutableStateOf(zeroInsets)
    private var maximizedState by mutableStateOf(false)

    private val procedure = ComposeWindowProcedure(
        window = window,
        hitTest = ::resolveHitTest,
        onWindowInsetUpdate = { insetsState = it },
        onWindowMaximizedChanged = { maximizedState = it },
    )

    override val isMaximized: Boolean
        get() = maximizedState

    override val windowInsets: WindowInsets
        get() = insetsState

    override fun updateTitleBarBounds(bounds: Rect) {
        titleBarBounds = bounds
    }

    override fun updateMinimizeButtonBounds(bounds: Rect) {
        minimizeButtonBounds = bounds
    }

    override fun updateMaximizeButtonBounds(bounds: Rect) {
        maximizeButtonBounds = bounds
    }

    override fun updateCloseButtonBounds(bounds: Rect) {
        closeButtonBounds = bounds
    }

    override fun setTitleBarHitTestOwner(hitTestOwner: DesktopWindowTitleBarHitTestOwner?) {
        titleBarHitTestOwner = hitTestOwner
    }

    override fun setTitleBarHitTestEnabled(enabled: Boolean) {
        titleBarHitTestEnabled = enabled
    }

    override fun minimize() {
        com.sun.jna.platform.win32.User32.INSTANCE.ShowWindow(procedure.windowHandle, WinUser.SW_MINIMIZE)
    }

    override fun toggleMaximize() {
        val showCmd = if (maximizedState) WinUser.SW_RESTORE else WinUser.SW_MAXIMIZE
        com.sun.jna.platform.win32.User32.INSTANCE.ShowWindow(procedure.windowHandle, showCmd)
    }

    private fun resolveHitTest(x: Float, y: Float): Int {
        return when {
            maximizeButtonBounds.containsPoint(x, y) -> WinUserConst.HTCLIENT
            minimizeButtonBounds.containsPoint(x, y) -> WinUserConst.HTMINBUTTON
            closeButtonBounds.containsPoint(x, y) -> WinUserConst.HTCLOSE
            titleBarHitTestEnabled &&
                titleBarBounds.containsPoint(x, y) &&
                titleBarHitTestOwner?.hitTest(x, y) != true -> WinUserConst.HTCAPTION
            else -> WinUserConst.HTCLIENT
        }
    }
}

private class ComposeWindowProcedure(
    private val window: Window,
    private val hitTest: (x: Float, y: Float) -> Int,
    private val onWindowInsetUpdate: (WindowInsets) -> Unit,
    private val onWindowMaximizedChanged: (Boolean) -> Unit,
) : WindowProcedure {
    private val windowPointer = (window as? ComposeWindow)?.windowHandle?.let(::Pointer) ?: Native.getWindowPointer(window)
    val windowHandle = HWND(windowPointer)

    private val margins = WindowMargins(
        leftBorderWidth = 0,
        topBorderHeight = 0,
        rightBorderWidth = -1,
        bottomBorderHeight = -1,
    )

    private val defaultWindowProcedure =
        User32Extend.instance?.setWindowLong(windowHandle, WinUser.GWL_WNDPROC, this) ?: LONG_PTR(-1)

    private var dpi = com.sun.jna.platform.win32.WinDef.UINT(0)
    private var width = 0
    private var height = 0
    private var frameX = 0
    private var frameY = 0
    private var edgeX = 0
    private var edgeY = 0
    private var padding = 0
    private var isMaximized = com.sun.jna.platform.win32.User32.INSTANCE.isWindowInMaximized(windowHandle)

    @Suppress("unused")
    private val skiaLayerProcedure = (window as? ComposeWindow)?.findSkiaLayer()?.let {
        SkiaLayerWindowProcedure(
            skiaLayer = it,
            hitTest = { x, y ->
                updateWindowInfo()
                resolveHitTest(x, y)
            },
        )
    }

    init {
        enableResizability()
        enableBorderAndShadow()
        onWindowMaximizedChanged(isMaximized)
    }

    override fun callback(hWnd: HWND, uMsg: Int, wParam: WPARAM, lParam: LPARAM): LRESULT {
        return when (uMsg) {
            WinUserConst.WM_NCCALCSIZE -> {
                if (wParam.toInt() == 0) {
                    defaultCall(hWnd, uMsg, wParam, lParam)
                } else {
                    val user32 = User32Extend.instance ?: return LRESULT(0)
                    dpi = user32.GetDpiForWindow(hWnd)
                    frameX = user32.GetSystemMetricsForDpi(WinUser.SM_CXFRAME, dpi)
                    frameY = user32.GetSystemMetricsForDpi(WinUser.SM_CYFRAME, dpi)
                    edgeX = user32.GetSystemMetricsForDpi(WinUser.SM_CXEDGE, dpi)
                    edgeY = user32.GetSystemMetricsForDpi(WinUser.SM_CYEDGE, dpi)
                    padding = user32.GetSystemMetricsForDpi(WinUser.SM_CXPADDEDBORDER, dpi)
                    isMaximized = user32.isWindowInMaximized(hWnd)
                    onWindowMaximizedChanged(isMaximized)
                    onWindowInsetUpdate(
                        WindowInsets(
                            left = if (isMaximized) frameX + padding else edgeX,
                            top = if (isMaximized) frameY + padding else edgeY,
                            right = if (isMaximized) frameX + padding else edgeX,
                            bottom = if (isMaximized) frameY + padding else edgeY,
                        ),
                    )
                    LRESULT(0)
                }
            }

            WinUserConst.WM_NCHITTEST -> {
                val result = lParam.useMousePoint(windowHandle) { x, y ->
                    updateWindowInfo()
                    resolveHitTest(x.toFloat(), y.toFloat())
                }
                LRESULT(result.toLong())
            }

            WM_SIZE -> {
                width = lParam.toInt().lowWord
                height = lParam.toInt().highWord
                isMaximized = com.sun.jna.platform.win32.User32.INSTANCE.isWindowInMaximized(hWnd)
                onWindowMaximizedChanged(isMaximized)
                defaultCall(hWnd, uMsg, wParam, lParam)
            }

            WM_DESTROY -> defaultCall(hWnd, uMsg, wParam, lParam)

            else -> defaultCall(hWnd, uMsg, wParam, lParam)
        }
    }

    private fun defaultCall(hWnd: HWND, uMsg: Int, wParam: WPARAM, lParam: LPARAM): LRESULT {
        return User32Extend.instance?.CallWindowProc(defaultWindowProcedure, hWnd, uMsg, wParam, lParam) ?: LRESULT(0)
    }

    private fun resolveHitTest(x: Float, y: Float): Int {
        val horizontalPadding = frameX
        val verticalPadding = frameY
        return when {
            !isMaximized && x <= horizontalPadding && y > verticalPadding && y < height - verticalPadding -> WinUserConst.HTLEFT
            !isMaximized && x <= horizontalPadding && y <= verticalPadding -> WinUserConst.HTTOPLEFT
            !isMaximized && x <= horizontalPadding -> WinUserConst.HTBOTTOMLEFT
            !isMaximized && y <= verticalPadding && x > horizontalPadding && x < width - horizontalPadding -> WinUserConst.HTTOP
            !isMaximized && y <= verticalPadding && x <= horizontalPadding -> WinUserConst.HTTOPLEFT
            !isMaximized && y <= verticalPadding -> WinUserConst.HTTOPRIGHT
            !isMaximized && x >= width - horizontalPadding && y > verticalPadding && y < height - verticalPadding -> WinUserConst.HTRIGHT
            !isMaximized && x >= width - horizontalPadding && y <= verticalPadding -> WinUserConst.HTTOPRIGHT
            !isMaximized && x >= width - horizontalPadding -> WinUserConst.HTBOTTOMRIGHT
            !isMaximized && y >= height - verticalPadding && x > horizontalPadding && x < width - horizontalPadding -> WinUserConst.HTBOTTOM
            !isMaximized && y >= height - verticalPadding && x <= horizontalPadding -> WinUserConst.HTBOTTOMLEFT
            !isMaximized && y >= height - verticalPadding -> WinUserConst.HTBOTTOMRIGHT
            else -> hitTest(x, y)
        }
    }

    private fun updateWindowInfo() {
        User32Extend.instance?.apply {
            dpi = GetDpiForWindow(windowHandle)
            frameX = GetSystemMetricsForDpi(WinUser.SM_CXFRAME, dpi)
            frameY = GetSystemMetricsForDpi(WinUser.SM_CYFRAME, dpi)
            val rect = RECT()
            if (GetWindowRect(windowHandle, rect)) {
                rect.read()
                width = rect.right - rect.left
                height = rect.bottom - rect.top
            }
            rect.clear()
        }
    }

    private fun enableResizability() {
        User32Extend.instance?.updateWindowStyle(windowHandle) { oldStyle ->
            (oldStyle or WinUser.WS_CAPTION) and WinUser.WS_SYSMENU.inv()
        }
    }

    private fun enableBorderAndShadow() {
        val dwmApi = runCatching { NativeLibrary.getInstance("dwmapi") }.getOrNull()
        dwmApi?.runCatching {
            getFunction("DwmExtendFrameIntoClientArea").invoke(arrayOf(windowHandle, margins))
        }
        if (isWindows11OrLater()) {
            dwmApi?.getFunction("DwmSetWindowAttribute")?.apply {
                invoke(WinNT.HRESULT::class.java, arrayOf(windowHandle, 35, IntByReference((0xFFFFFFFE).toInt()), 4))
                invoke(WinNT.HRESULT::class.java, arrayOf(windowHandle, 38, IntByReference(2), 4))
            }
        }
    }
}

private class SkiaLayerWindowProcedure(
    skiaLayer: SkiaLayer,
    private val hitTest: (x: Float, y: Float) -> Int,
) : WindowProcedure {
    private val windowHandle = HWND(Pointer(skiaLayer.windowHandle))
    private val contentHandle = HWND(skiaLayer.canvas.let(Native::getComponentPointer))
    private val defaultWindowProcedure =
        User32Extend.instance?.setWindowLong(contentHandle, WinUser.GWL_WNDPROC, this) ?: LONG_PTR(-1)

    override fun callback(hwnd: HWND, uMsg: Int, wParam: WPARAM, lParam: LPARAM): LRESULT {
        return when (uMsg) {
            WinUserConst.WM_NCHITTEST -> {
                val result = lParam.useMousePoint(windowHandle) { x, y -> hitTest(x.toFloat(), y.toFloat()) }
                when (result) {
                    WinUserConst.HTCLIENT,
                    WinUserConst.HTMINBUTTON,
                    WinUserConst.HTCLOSE,
                    -> LRESULT(result.toLong())

                    else -> LRESULT(WinUserConst.HTTRANSPARENT.toLong())
                }
            }

            WinUserConst.WM_NCMOUSEMOVE -> {
                User32Extend.instance?.SendMessage(contentHandle, WinUserConst.WM_MOUSEMOVE, wParam, lParam)
                LRESULT(0)
            }

            WinUserConst.WM_NCLBUTTONDOWN -> {
                User32Extend.instance?.SendMessage(contentHandle, WinUserConst.WM_LBUTTONDOWN, wParam, lParam)
                LRESULT(0)
            }

            WinUserConst.WM_NCLBUTTONUP -> {
                User32Extend.instance?.SendMessage(contentHandle, WinUserConst.WM_LBUTTONUP, wParam, lParam)
                LRESULT(0)
            }

            else -> User32Extend.instance?.CallWindowProc(defaultWindowProcedure, hwnd, uMsg, wParam, lParam) ?: LRESULT(0)
        }
    }
}

private inline fun <T> LPARAM.useMousePoint(windowHandle: HWND, block: (x: Int, y: Int) -> T): T {
    val point = POINT(toInt().lowWord.toShort().toInt(), toInt().highWord.toShort().toInt())
    User32Extend.instance?.ScreenToClient(windowHandle, point)
    point.read()
    val result = block(point.x, point.y)
    point.clear()
    return result
}

private fun <T : JComponent> findComponent(container: Container, klass: Class<T>): T? {
    val components = container.components.asSequence()
    return components.filter { klass.isInstance(it) }
        .ifEmpty { components.filterIsInstance<Container>().mapNotNull { findComponent(it, klass) } }
        .map { klass.cast(it) }
        .firstOrNull()
}

private inline fun <reified T : JComponent> Container.findComponent(): T? = findComponent(this, T::class.java)

private fun ComposeWindow.findSkiaLayer(): SkiaLayer? = findComponent()

private fun Rect.containsPoint(x: Float, y: Float): Boolean {
    return x >= left && x < right && y >= top && y < bottom
}
