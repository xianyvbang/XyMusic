package cn.xybbz.common.utils

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.math.abs

class ResourcesUtilsTest {

    @Test
    fun sampleDominantColorPrefersAccentOverNearWhiteBackground() {
        val accent = argb(230, 54, 87)

        val dominant = sampleDominantColor(width = 24, height = 24) { x, y ->
            if (x in 8..15 && y in 8..15) accent else argb(250, 250, 250)
        }

        assertColorClose(dominant, red = 230, green = 54, blue = 87)
    }

    @Test
    fun sampleDominantColorPrefersAccentOverNearBlackBackground() {
        val accent = argb(233, 179, 0)

        val dominant = sampleDominantColor(width = 24, height = 24) { x, y ->
            if (x in 8..15 && y in 8..15) accent else argb(12, 12, 12)
        }

        assertColorClose(dominant, red = 233, green = 179, blue = 0)
    }

    @Test
    fun sanitizePaletteColorForThemeReturnsTransparentForNearBlackColorInLightTheme() {
        val adjusted = sanitizePaletteColorForTheme(
            color = Color(0xFF121212),
            isDarkTheme = false
        )

        assertEquals(Color.Transparent, adjusted)
    }

    @Test
    fun sanitizePaletteColorForThemeKeepsNearBlackColorInDarkTheme() {
        val original = Color(0xFF121212)
        val adjusted = sanitizePaletteColorForTheme(
            color = original,
            isDarkTheme = true
        )

        assertEquals(original, adjusted)
    }

    @Test
    fun sanitizePaletteColorForThemeReturnsTransparentForNearWhiteColorInDarkTheme() {
        val adjusted = sanitizePaletteColorForTheme(
            color = Color(0xFFF7F7F7),
            isDarkTheme = true
        )

        assertEquals(Color.Transparent, adjusted)
    }

    private fun assertColorClose(color: Color, red: Int, green: Int, blue: Int) {
        assertTrue(abs(color.red * 255 - red) <= 2f)
        assertTrue(abs(color.green * 255 - green) <= 2f)
        assertTrue(abs(color.blue * 255 - blue) <= 2f)
    }

    private fun argb(red: Int, green: Int, blue: Int): Int {
        return (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
    }
}
