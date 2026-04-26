package cn.xybbz.ui.windows

import com.sun.jna.platform.win32.WinUser

internal object WindowsChromeConstants {
    const val WM_NCCALCSIZE: Int = 0x0083
    const val WM_NCHITTEST: Int = 0x0084
    const val WM_GETMINMAXINFO: Int = 0x0024
    const val WM_NCLBUTTONUP: Int = 0x00A2

    const val HTTRANSPARENT: Int = -1
    const val HTCLIENT: Int = 1
    const val HTCAPTION: Int = 2
    const val HTLEFT: Int = 10
    const val HTRIGHT: Int = 11
    const val HTTOP: Int = 12
    const val HTTOPLEFT: Int = 13
    const val HTTOPRIGHT: Int = 14
    const val HTBOTTOM: Int = 15
    const val HTBOTTOMLEFT: Int = 16
    const val HTBOTTOMRIGHT: Int = 17
    const val HTMAXBUTTON: Int = 9

    const val SC_CLOSE: Int = 0xF060
    const val SC_RESTORE: Int = 0xF120

    const val SM_CYFRAME: Int = 33
    const val SM_CXPADDEDBORDER: Int = 92

    const val FRAME_STYLE: Int =
        WinUser.WS_CAPTION or
            WinUser.WS_THICKFRAME or
            WinUser.WS_MINIMIZEBOX or
            WinUser.WS_MAXIMIZEBOX or
            WinUser.WS_SYSMENU
}
