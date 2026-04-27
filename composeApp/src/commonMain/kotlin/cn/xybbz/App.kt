package cn.xybbz

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.router.Navigator
import cn.xybbz.router.platformNavigationConfig
import cn.xybbz.router.rememberNavigationState
import cn.xybbz.ui.popup.XyPopTipHost
import cn.xybbz.ui.screens.MainScreen
import cn.xybbz.ui.theme.XyConfigs
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.theme.xyBackgroundBrash
import org.koin.compose.getKoin

@Composable
//@Preview
fun App(
    windowContentPadding: PaddingValues = PaddingValues(),
) {

    val settingsManager: SettingsManager = getKoin().get()
    val themeType by settingsManager.themeType.collectAsState()
    val imageFilePath by settingsManager.imageFilePath.collectAsState()
    val navigationConfig = platformNavigationConfig
    val navigationState = rememberNavigationState(
        startRoute = navigationConfig.startRoute,
        topLevelRoutes = navigationConfig.topLevelRoutes
    )
    val navigator = remember(navigationState) {
        Navigator(navigationState)
    }

    val isDark = when (themeType) {
        ThemeTypeEnum.SYSTEM -> isSystemInDarkTheme()
        ThemeTypeEnum.DARK -> true
        ThemeTypeEnum.LIGHT -> false
    }

    XyTheme(
        xyConfigs = XyConfigs(
            isDarkTheme = isDark,
        ),
        brash = xyBackgroundBrash(
            backgroundImageUri = imageFilePath
        )
    ) {
        CompositionLocalProvider(LocalNavigator provides navigator) {
            WindowInsets.systemBars.union(WindowInsets.displayCutout)
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(windowContentPadding)
                ) {
                    MainScreen(
                        navigationConfig = navigationConfig,
                        navigationState = navigationState,
                        navigator = navigator,
                    )
                    XyPopTipHost()
                }
            }
        }
    }
}
