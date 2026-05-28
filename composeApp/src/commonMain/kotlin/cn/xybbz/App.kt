package cn.xybbz

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.router.Navigator
import cn.xybbz.router.RootNavTransition
import cn.xybbz.router.platformNavigationConfig
import cn.xybbz.router.rememberNavigationState
import cn.xybbz.ui.popup.XyPopTipHost
import cn.xybbz.ui.screens.ConnectionScreen
import cn.xybbz.ui.screens.MainScreen
import cn.xybbz.ui.theme.XyConfigs
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.theme.xyBackgroundBrash
import cn.xybbz.viewmodel.StartupState
import cn.xybbz.viewmodel.StartupViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * 启动后外层可切换的根内容。
 * 启动加载页已经从路由枚举里拆出去了，剩下的只负责连接页和主壳切换。
 */
enum class AppStartupContent {
    CONNECTION,
    MAIN
}

@Composable
//@Preview
fun App(
    windowContentPadding: PaddingValues = PaddingValues(),
) {
    // 启动状态单独由轻量 ViewModel 管理，避免首帧阶段提前创建 MainViewModel。
    val settingsManager = koinInject<SettingsManager>()
    val startupViewModel = koinViewModel<StartupViewModel>()
    // 主题读好再进入 APP，防止黑白背景闪烁。
    val appState = startupViewModel.appState.collectAsStateWithLifecycle().value
    val navigationConfig = platformNavigationConfig
    val navigationState = rememberNavigationState(
        startRoute = navigationConfig.startRoute,
        topLevelRoutes = navigationConfig.topLevelRoutes,
        enableTopLevelRoutes = navigationConfig.enableTopLevelRoutes
    )
    val navigator = remember(navigationState) {
        Navigator(navigationState)
    }
    val systemInDarkTheme = isSystemInDarkTheme()

    val isDark = when (appState.themeTypeEnum) {
        ThemeTypeEnum.SYSTEM -> {
            systemInDarkTheme
        }
        ThemeTypeEnum.DARK -> true
        ThemeTypeEnum.LIGHT -> false
    }

    XyTheme(
        xyConfigs = XyConfigs(
            isDarkTheme = isDark,
        ),
        brash = xyBackgroundBrash(
            backgroundImageUri = appState.imageFilePath
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
                    RootNavTransition(
                        state = appState.mainSceneInitialPage,
                        enableAnimations = navigationConfig.enableAnimations
                    ) { content ->
                        when (content) {
                            AppStartupContent.CONNECTION -> {
                                // 首次打开直接渲染连接页，不创建 MainScreen/MainViewModel，避免拉起播放器等重依赖。
                                ConnectionScreen(connectionUiType = ConnectionUiType.FIRST_OPEN)
                            }
                            else -> {
                                // 只有 readyForContent 后才进入 MAIN，动画切换不会提前创建主壳重依赖。
                                MainScreen(
                                    navigationConfig = navigationConfig,
                                    navigationState = navigationState,
                                    navigator = navigator,
                                )
                            }
                        }
                    }
                    XyPopTipHost()
                }
            }
        }
    }
}
