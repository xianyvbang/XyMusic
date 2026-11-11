package cn.xybbz.ui.theme

/**
 * Android 端调色板：与 iOS 共用移动端配色，保持小屏高对比的视觉。
 */
internal actual fun platformLightPalette(): XyPalette = mobileLightPalette()

internal actual fun platformDarkPalette(): XyPalette = mobileDarkPalette()

internal actual fun platformExtendedColors(isDarkTheme: Boolean): XyExtendedColors =
    mobileExtendedColors(isDarkTheme)