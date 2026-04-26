package cn.xybbz.ui.windows

import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.win32.StdCallLibrary

internal interface DwmApi : StdCallLibrary {
    fun DwmExtendFrameIntoClientArea(
        hWnd: HWND,
        margins: DwmMargins,
    ): HRESULT
}
