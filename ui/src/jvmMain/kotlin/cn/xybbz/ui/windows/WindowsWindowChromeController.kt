package cn.xybbz.ui.windows

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Rect
import com.sun.jna.CallbackReference
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.RECT
import com.sun.jna.platform.win32.WinDef.WPARAM
import com.sun.jna.platform.win32.WinUser
import java.awt.Frame
import kotlin.math.max

internal class WindowsWindowChromeController(
    private val window: ComposeWindow,
    private val onCloseRequest: () -> Unit,
) : DesktopWindowChromeController, WinUser.WindowProc {
    private val user32 = User32.INSTANCE
    private var hwnd: HWND? = null
    private var originalWndProc: Pointer? = null
    private var callbackPointer: Pointer? = null
    private var installed = false

    private var titleBarBounds: Rect = Rect.Zero
    private var minimizeButtonBounds: Rect = Rect.Zero
    private var maximizeButtonBounds: Rect = Rect.Zero
    private var closeButtonBounds: Rect = Rect.Zero
    private var maximizedState by mutableStateOf(false)

    override val isMaximized: Boolean
        get() = maximizedState

    override val windowInsets: WindowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0)

    override var titleBarHitTestOwner: DesktopWindowTitleBarHitTestOwner? = null
        private set

    override var isTitleBarHitTestEnabled: Boolean = false
        private set

    fun install() {
        if (installed) return

        val componentPointer = Native.getComponentPointer(window) ?: return
        val nativeHwnd = HWND(componentPointer)
        hwnd = nativeHwnd
        syncMaximizedState()
        applyWindowStyle(nativeHwnd)
        extendFrameIntoClientArea(nativeHwnd)

        originalWndProc = user32.GetWindowLongPtr(nativeHwnd, WinUser.GWL_WNDPROC).toPointer()
        callbackPointer = CallbackReference.getFunctionPointer(this)
        user32.SetWindowLongPtr(nativeHwnd, WinUser.GWL_WNDPROC, callbackPointer)
        installed = true
    }

    fun dispose() {
        val nativeHwnd = hwnd
        val previousWndProc = originalWndProc
        if (installed && nativeHwnd != null && previousWndProc != null) {
            user32.SetWindowLongPtr(nativeHwnd, WinUser.GWL_WNDPROC, previousWndProc)
        }
        installed = false
        hwnd = null
        originalWndProc = null
        callbackPointer = null
        titleBarHitTestOwner = null
        isTitleBarHitTestEnabled = false
    }

    override fun callback(
        hwnd: HWND,
        uMsg: Int,
        wParam: WPARAM,
        lParam: LPARAM,
    ): LRESULT {
        return when (uMsg) {
            WindowsChromeConstants.WM_NCCALCSIZE -> {
                if (wParam.toLong() != 0L) {
                    LRESULT(0)
                } else {
                    callOriginal(hwnd, uMsg, wParam, lParam)
                }
            }
            WindowsChromeConstants.WM_NCHITTEST -> LRESULT(hitTest(lParam).toLong())
            WindowsChromeConstants.WM_GETMINMAXINFO -> {
                updateMinMaxInfo(hwnd, lParam)
                LRESULT(0)
            }
            WinUser.WM_SIZE -> {
                syncMaximizedState()
                callOriginal(hwnd, uMsg, wParam, lParam)
            }
            WindowsChromeConstants.WM_NCLBUTTONUP -> {
                if (wParam.toInt() == WindowsChromeConstants.HTMAXBUTTON) {
                    toggleMaximize()
                    LRESULT(0)
                } else {
                    callOriginal(hwnd, uMsg, wParam, lParam)
                }
            }
            WinUser.WM_SYSCOMMAND -> {
                if ((wParam.toInt() and 0xFFF0) == WindowsChromeConstants.SC_CLOSE) {
                    close()
                    LRESULT(0)
                } else {
                    callOriginal(hwnd, uMsg, wParam, lParam)
                }
            }
            else -> callOriginal(hwnd, uMsg, wParam, lParam)
        }
    }

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
        isTitleBarHitTestEnabled = enabled
    }

    override fun minimize() {
        hwnd?.let { nativeHwnd ->
            user32.CloseWindow(nativeHwnd)
        }
    }

    override fun toggleMaximize() {
        val nativeHwnd = hwnd ?: return
        val command = if (isMaximized) {
            WindowsChromeConstants.SC_RESTORE
        } else {
            WinUser.SC_MAXIMIZE
        }
        user32.SendMessage(
            nativeHwnd,
            WinUser.WM_SYSCOMMAND,
            WPARAM(command.toLong()),
            LPARAM(0)
        )
        syncMaximizedState()
    }

    override fun close() {
        onCloseRequest()
    }

    private fun applyWindowStyle(nativeHwnd: HWND) {
        val style = user32.GetWindowLong(nativeHwnd, WinUser.GWL_STYLE)
        user32.SetWindowLong(
            nativeHwnd,
            WinUser.GWL_STYLE,
            style or WindowsChromeConstants.FRAME_STYLE
        )
        user32.SetWindowPos(
            nativeHwnd,
            null,
            0,
            0,
            0,
            0,
            WinUser.SWP_NOMOVE or
                WinUser.SWP_NOSIZE or
                WinUser.SWP_NOZORDER or
                WinUser.SWP_FRAMECHANGED
        )
    }

    private fun extendFrameIntoClientArea(nativeHwnd: HWND) {
        runCatching {
            DwmApiProvider.instance.DwmExtendFrameIntoClientArea(
                nativeHwnd,
                DwmMargins(cyTopHeight = 1)
            )
        }
    }

    private fun hitTest(lParam: LPARAM): Int {
        val nativeHwnd = hwnd ?: return WindowsChromeConstants.HTCLIENT
        val screenX = signedLowWord(lParam.toLong())
        val screenY = signedHighWord(lParam.toLong())
        val windowRect = RECT()
        user32.GetWindowRect(nativeHwnd, windowRect)

        if (!isMaximized) {
            val resizeHit = hitTestResizeBorder(screenX, screenY, windowRect)
            if (resizeHit != WindowsChromeConstants.HTCLIENT) {
                return resizeHit
            }
        }

        val locationOnScreen = runCatching { window.locationOnScreen }.getOrNull()
            ?: return WindowsChromeConstants.HTCLIENT
        val clientX = (screenX - locationOnScreen.x).toFloat()
        val clientY = (screenY - locationOnScreen.y).toFloat()

        if (maximizeButtonBounds.contains(clientX, clientY)) {
            return WindowsChromeConstants.HTMAXBUTTON
        }
        if (
            minimizeButtonBounds.contains(clientX, clientY) ||
            closeButtonBounds.contains(clientX, clientY)
        ) {
            return WindowsChromeConstants.HTCLIENT
        }

        if (!titleBarBounds.contains(clientX, clientY)) {
            return WindowsChromeConstants.HTCLIENT
        }

        if (
            isTitleBarHitTestEnabled &&
            titleBarHitTestOwner?.hitTest(clientX, clientY) == true
        ) {
            return WindowsChromeConstants.HTCLIENT
        }

        return WindowsChromeConstants.HTCAPTION
    }

    private fun hitTestResizeBorder(
        screenX: Int,
        screenY: Int,
        windowRect: RECT,
    ): Int {
        val frameSize = resizeBorderSize()
        val onLeft = screenX >= windowRect.left && screenX < windowRect.left + frameSize
        val onRight = screenX < windowRect.right && screenX >= windowRect.right - frameSize
        val onTop = screenY >= windowRect.top && screenY < windowRect.top + frameSize
        val onBottom = screenY < windowRect.bottom && screenY >= windowRect.bottom - frameSize

        return when {
            onTop && onLeft -> WindowsChromeConstants.HTTOPLEFT
            onTop && onRight -> WindowsChromeConstants.HTTOPRIGHT
            onBottom && onLeft -> WindowsChromeConstants.HTBOTTOMLEFT
            onBottom && onRight -> WindowsChromeConstants.HTBOTTOMRIGHT
            onLeft -> WindowsChromeConstants.HTLEFT
            onRight -> WindowsChromeConstants.HTRIGHT
            onTop -> WindowsChromeConstants.HTTOP
            onBottom -> WindowsChromeConstants.HTBOTTOM
            else -> WindowsChromeConstants.HTCLIENT
        }
    }

    private fun updateMinMaxInfo(nativeHwnd: HWND, lParam: LPARAM) {
        val monitor = user32.MonitorFromWindow(nativeHwnd, WinUser.MONITOR_DEFAULTTONEAREST)
            ?: return
        val monitorInfo = WinUser.MONITORINFO()
        if (!user32.GetMonitorInfo(monitor, monitorInfo).booleanValue()) {
            return
        }

        val monitorRect = monitorInfo.rcMonitor
        val workRect = monitorInfo.rcWork
        // LPARAM.toPointer() creates an opaque constant pointer; Structure needs a shareable pointer.
        val minMaxInfo = MinMaxInfo(Pointer(lParam.toLong())).apply { read() }
        minMaxInfo.ptMaxPosition.x = workRect.left - monitorRect.left
        minMaxInfo.ptMaxPosition.y = workRect.top - monitorRect.top
        minMaxInfo.ptMaxSize.x = workRect.right - workRect.left
        minMaxInfo.ptMaxSize.y = workRect.bottom - workRect.top
        minMaxInfo.ptMaxTrackSize.x = minMaxInfo.ptMaxSize.x
        minMaxInfo.ptMaxTrackSize.y = minMaxInfo.ptMaxSize.y
        minMaxInfo.write()
    }

    private fun callOriginal(
        hwnd: HWND,
        uMsg: Int,
        wParam: WPARAM,
        lParam: LPARAM,
    ): LRESULT {
        val previousWndProc = originalWndProc
        return if (previousWndProc != null && Pointer.nativeValue(previousWndProc) != 0L) {
            user32.CallWindowProc(previousWndProc, hwnd, uMsg, wParam, lParam)
        } else {
            user32.DefWindowProc(hwnd, uMsg, wParam, lParam)
        }
    }

    private fun resizeBorderSize(): Int {
        return max(
            user32.GetSystemMetrics(WinUser.SM_CXSIZEFRAME),
            user32.GetSystemMetrics(WindowsChromeConstants.SM_CYFRAME)
        ) + user32.GetSystemMetrics(WindowsChromeConstants.SM_CXPADDEDBORDER)
    }

    private fun syncMaximizedState() {
        maximizedState = (window.extendedState and Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH
    }

    private fun Rect.contains(x: Float, y: Float): Boolean {
        return x >= left && x < right && y >= top && y < bottom
    }

    private fun signedLowWord(value: Long): Int {
        return (value and 0xFFFF).toShort().toInt()
    }

    private fun signedHighWord(value: Long): Int {
        return ((value shr 16) and 0xFFFF).toShort().toInt()
    }
}
