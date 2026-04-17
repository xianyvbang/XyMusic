package cn.xybbz.window

import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.Structure
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.POINT
import com.sun.jna.platform.win32.WinDef.UINT
import com.sun.jna.platform.win32.WinDef.WPARAM
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.platform.win32.WinUser.WindowProc
import com.sun.jna.win32.W32APIOptions

internal object WinUserConst {
    const val WM_NCCALCSIZE = 0x0083
    const val WM_NCHITTEST = 0x0084
    const val WM_MOUSEMOVE = 0x0200
    const val WM_LBUTTONDOWN = 0x0201
    const val WM_LBUTTONUP = 0x0202
    const val WM_NCMOUSEMOVE = 0x00A0
    const val WM_NCLBUTTONDOWN = 0x00A1
    const val WM_NCLBUTTONUP = 0x00A2

    const val HTTRANSPARENT = -1
    const val HTCLIENT = 1
    const val HTCAPTION = 2
    const val HTMINBUTTON = 8
    const val HTMAXBUTTON = 9
    const val HTLEFT = 10
    const val HTRIGHT = 11
    const val HTTOP = 12
    const val HTTOPLEFT = 13
    const val HTTOPRIGHT = 14
    const val HTBOTTOM = 15
    const val HTBOTTOMLEFT = 16
    const val HTBOTTOMRIGHT = 17
    const val HTCLOSE = 20
}

@Structure.FieldOrder(
    "leftBorderWidth",
    "rightBorderWidth",
    "topBorderHeight",
    "bottomBorderHeight",
)
internal data class WindowMargins(
    @JvmField var leftBorderWidth: Int,
    @JvmField var rightBorderWidth: Int,
    @JvmField var topBorderHeight: Int,
    @JvmField var bottomBorderHeight: Int,
) : Structure(), Structure.ByReference

internal val windowsBuildNumber by lazy {
    Kernel32.INSTANCE.GetVersion().high.toInt()
}

internal fun isWindows11OrLater(): Boolean = windowsBuildNumber >= 22000

@Suppress("FunctionName")
internal interface User32Extend : User32 {
    fun SetWindowLong(hWnd: HWND, nIndex: Int, wndProc: WindowProc): LONG_PTR

    fun SetWindowLongPtr(hWnd: HWND, nIndex: Int, wndProc: WindowProc): LONG_PTR

    fun CallWindowProc(
        proc: LONG_PTR,
        hWnd: HWND,
        uMsg: Int,
        wParam: WPARAM,
        lParam: LPARAM,
    ): LRESULT

    fun GetSystemMetricsForDpi(nIndex: Int, dpi: UINT): Int

    fun GetDpiForWindow(hWnd: HWND): UINT

    fun ScreenToClient(hWnd: HWND, point: POINT): Boolean

    companion object {
        val instance by lazy {
            runCatching {
                Native.load(
                    "user32",
                    User32Extend::class.java,
                    W32APIOptions.DEFAULT_OPTIONS,
                )
            }.getOrNull()
        }
    }
}

internal fun User32Extend.setWindowLong(hWnd: HWND, nIndex: Int, procedure: WindowProcedure): LONG_PTR {
    return if (Platform.is64Bit()) {
        SetWindowLongPtr(hWnd, nIndex, procedure)
    } else {
        SetWindowLong(hWnd, nIndex, procedure)
    }
}

internal fun User32.isWindowInMaximized(hWnd: HWND): Boolean {
    val placement = WinUser.WINDOWPLACEMENT()
    val result = GetWindowPlacement(hWnd, placement).booleanValue() &&
        placement.showCmd == WinUser.SW_SHOWMAXIMIZED
    placement.clear()
    return result
}

internal fun User32.updateWindowStyle(hWnd: HWND, block: (oldStyle: Int) -> Int) {
    val oldStyle = GetWindowLong(hWnd, WinUser.GWL_STYLE)
    SetWindowLong(hWnd, WinUser.GWL_STYLE, block(oldStyle))
}

internal val Int.lowWord: Int
    get() = this and 0xFFFF

internal val Int.highWord: Int
    get() = (this shr 16) and 0xFFFF
