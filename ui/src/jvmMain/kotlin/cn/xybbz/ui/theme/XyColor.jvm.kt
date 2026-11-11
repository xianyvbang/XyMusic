package cn.xybbz.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 桌面端浅色调色板。
 *
 * 与移动端保持同一品牌蓝，但降低饱和度：大窗口下大面积高饱和色容易刺眼。
 * 中性底色沿用移动端的无彩色体系，只做轻微偏冷的桌面化处理。
 */
internal actual fun platformLightPalette(): XyPalette = XyPalette(
    primary = Color(0xFF2F70B1),
    onSurface = Color(0xFF1D1F22),
    onSurfaceVariant = Color(0xFF71757C),
    surface = Color(0xFFF4F5F7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
)

/**
 * 桌面端深色调色板。
 *
 * 主色比移动端更亮、更低饱和，保证深色背景下可读性；正文与背景维持中性灰。
 */
internal actual fun platformDarkPalette(): XyPalette = XyPalette(
    primary = Color(0xFF4D93CB),
    onSurface = Color(0xFFE8EAED),
    onSurfaceVariant = Color(0xFF9CA1A8),
    surface = Color(0xFF1B1D20),
    surfaceContainerLowest = Color(0xFF2B2E32),
)

/**
 * 桌面端列表封面占位配色。
 *
 * 由歌曲 id 计算索引取色，用于无封面时给列表一点色彩层次。
 * 与主题明暗无关，因此不放进 [XyExtendedColors]，避免无意义的主题切换重算。
 */
val jvmCoverAccentPalette: List<Color> = listOf(
    Color(0xFF4A5BB8),
    Color(0xFFC05A50),
    Color(0xFFA08141),
    Color(0xFF3A7568),
    Color(0xFF66518C),
    Color(0xFF4E7059),
)

/**
 * 桌面端扩展颜色。
 */
internal actual fun platformExtendedColors(isDarkTheme: Boolean): XyExtendedColors =
    if (isDarkTheme) darkExtendedColors() else lightExtendedColors()

/**
 * 桌面端浅色主题扩展颜色。
 */
private fun lightExtendedColors(): XyExtendedColors {
    val palette = platformLightPalette()
    return XyExtendedColors(
        sidebarBackground = Color(0xFFECEEF2),
        sidebarNavigationSelectedBackground = palette.primary.copy(alpha = 0.12f),
        sidebarPlaylistSelectedBackground = palette.primary.copy(alpha = 0.08f),
        success = Color(0xFF2E7A57),
        successContainer = Color(0xFFE9F4EE),
        warning = Color(0xFF9A6B22),
        warningContainer = Color(0xFFFAF1E1),
        danger = Color(0xFFA84A44),
        favorite = Color(0xFFD05264),
        mediaCoverGradientStart = Color(0xFF4E76B8),
        mediaCoverGradientEnd = Color(0xFF77659E),
        artistCoverGradientStart = Color(0xFF3C8F6E),
        artistCoverGradientEnd = Color(0xFF3A8A9C),
    )
}

/**
 * 桌面端深色主题扩展颜色。
 */
private fun darkExtendedColors(): XyExtendedColors = XyExtendedColors(
    sidebarBackground = Color(0xFF141618),
    sidebarNavigationSelectedBackground = Color(0xFF23262A),
    sidebarPlaylistSelectedBackground = Color.White.copy(alpha = 0.05f),
    success = Color(0xFF4FA37A),
    successContainer = Color(0xFF1E2C25),
    warning = Color(0xFFD2A24E),
    warningContainer = Color(0xFF2B2418),
    danger = Color(0xFFCF6B62),
    favorite = Color(0xFFD96373),
    mediaCoverGradientStart = Color(0xFF4E76B8),
    mediaCoverGradientEnd = Color(0xFF77659E),
    artistCoverGradientStart = Color(0xFF3C8F6E),
    artistCoverGradientEnd = Color(0xFF3A8A9C),
)