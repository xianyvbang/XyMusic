package cn.xybbz.ui.windows

import com.sun.jna.Native
import com.sun.jna.win32.W32APIOptions

internal object DwmApiProvider {
    val instance: DwmApi by lazy {
        Native.load("dwmapi", DwmApi::class.java, W32APIOptions.DEFAULT_OPTIONS)
    }
}
