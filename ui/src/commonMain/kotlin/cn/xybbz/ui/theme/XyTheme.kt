/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalXyDimens = staticCompositionLocalOf { xyDimens() }
private val LocalXyConfigs = staticCompositionLocalOf { xyConfigs() }
val LocalXyBackgroundBrash = staticCompositionLocalOf { xyBackgroundBrash() }

/**
 * 扩展颜色未提供时的兜底值，使用移动端浅色扩展配色。
 */
private val LocalXyExtendedColors = staticCompositionLocalOf {
    mobileExtendedColors(isDarkTheme = false)
}

/**
 * 把平台调色板转换为 Material3 的深色 ColorScheme。
 */
private fun XyPalette.toDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = primary,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    surface = surface,
    background = surface,
    surfaceContainerLowest = surfaceContainerLowest
)

/**
 * 把平台调色板转换为 Material3 的浅色 ColorScheme。
 */
private fun XyPalette.toLightColorScheme(): ColorScheme = lightColorScheme(
    primary = primary,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    surface = surface,
    background = surface,
    surfaceContainerLowest = surfaceContainerLowest
)

@Composable
fun XyTheme(
    xyConfigs: XyConfigs = XyTheme.configs,
    dimens: XyDimens = XyTheme.dimens,
    brash: XyBackgroundBrash = XyTheme.brash,
    content: @Composable () -> Unit
) {

    val isDarkTheme = xyConfigs.isDarkTheme
    // 按平台与明暗主题取调色板，移动端与桌面端各自维护自己的品牌色
    val colorScheme = remember(isDarkTheme) {
        val palette = if (isDarkTheme) platformDarkPalette() else platformLightPalette()
        if (isDarkTheme) palette.toDarkColorScheme() else palette.toLightColorScheme()
    }
    // 扩展颜色随主题明暗切换，供侧栏、状态提示、封面渐变等色位使用
    val extendedColors = remember(isDarkTheme) { platformExtendedColors(isDarkTheme) }

    CompositionLocalProvider(
        LocalXyDimens provides dimens,
        LocalXyConfigs provides xyConfigs,
        LocalXyBackgroundBrash provides brash,
        LocalXyExtendedColors provides extendedColors,
    ) {

        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }

}


object XyTheme {

    val dimens: XyDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalXyDimens.current

    val configs: XyConfigs
        @Composable
        @ReadOnlyComposable
        get() = LocalXyConfigs.current

    val brash: XyBackgroundBrash
        @Composable
        @ReadOnlyComposable
        get() = LocalXyBackgroundBrash.current

    /**
     * 扩展颜色：侧栏、状态提示、封面渐变等 Material3 未覆盖的色位。
     */
    val extendedColors: XyExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalXyExtendedColors.current
}