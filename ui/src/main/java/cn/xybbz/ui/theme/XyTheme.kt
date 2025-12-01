package cn.xybbz.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

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
            xyConfigs.isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (xyConfigs.isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                    context
                )
            }
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