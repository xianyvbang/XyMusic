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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalXyDimens = staticCompositionLocalOf { xyDimens() }
private val LocalXyConfigs = staticCompositionLocalOf { xyConfigs() }
val LocalXyBackgroundBrash = staticCompositionLocalOf { xyBackgroundBrash() }

private val DarkColorScheme = darkColorScheme(
    primary = darkPrimary,
    onSurface = darkOnSurface,
    onSurfaceVariant = darkOnSurfaceVariant,
    surface = darkSurface,
    background = darkSurface,
    surfaceContainerLowest = darkSurfaceContainerLowest
)

private val LightColorScheme = lightColorScheme(
    primary = lightPrimary,
    onSurface = lightOnSurface,
    onSurfaceVariant = lightOnSurfaceVariant,
    surface = lightSurface,
    background = lightSurface,
    surfaceContainerLowest = lightSurfaceContainerLowest
)

@Composable
fun XyTheme(
    xyConfigs: XyConfigs = XyTheme.configs,
    dimens: XyDimens = XyTheme.dimens,
    brash: XyBackgroundBrash = XyTheme.brash,
    content: @Composable () -> Unit
) {

    CompositionLocalProvider(
        LocalXyDimens provides dimens,
        LocalXyConfigs provides xyConfigs,
        LocalXyBackgroundBrash provides brash,
    ) {

        val colorScheme = when {
            xyConfigs.isDarkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
        /*val view = LocalView.current
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()*/
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
}