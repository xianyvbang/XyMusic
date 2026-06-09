package cn.xybbz.ui.windows

import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.win32.WinDef.POINT

@Structure.FieldOrder("ptReserved", "ptMaxSize", "ptMaxPosition", "ptMinTrackSize", "ptMaxTrackSize")
internal class MinMaxInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var ptReserved: POINT = POINT()
    @JvmField var ptMaxSize: POINT = POINT()
    @JvmField var ptMaxPosition: POINT = POINT()
    @JvmField var ptMinTrackSize: POINT = POINT()
    @JvmField var ptMaxTrackSize: POINT = POINT()
}
