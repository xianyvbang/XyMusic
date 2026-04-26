package cn.xybbz.ui.windows

import com.sun.jna.Structure

@Structure.FieldOrder("cxLeftWidth", "cxRightWidth", "cyTopHeight", "cyBottomHeight")
internal class DwmMargins(
    @JvmField var cxLeftWidth: Int = 0,
    @JvmField var cxRightWidth: Int = 0,
    @JvmField var cyTopHeight: Int = 0,
    @JvmField var cyBottomHeight: Int = 0,
) : Structure()
