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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.router.Navigator
import cn.xybbz.router.RootNavTransition
import cn.xybbz.router.platformNavigationConfig
import cn.xybbz.router.rememberNavigationState
import cn.xybbz.ui.popup.XyPopTipHost
import cn.xybbz.ui.screens.ConnectionScreen
import cn.xybbz.ui.screens.MainScreen
import cn.xybbz.ui.screens.StartupScreen
import cn.xybbz.ui.theme.XyConfigs
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.theme.xyBackgroundBrash
import cn.xybbz.viewmodel.StartupViewModel
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel

/**
 * 启动外层可切换的三种内容。
 * 用枚举驱动 RootNavTransition，避免把启动页/连接页/主壳强行压成 Boolean 状态。
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
    val startupViewModel = koinViewModel<StartupViewModel>()
    // 主题读好再进入 APP, 防止黑白背景闪烁
    val appState = startupViewModel.appState.collectAsStateWithLifecycle(null).value ?: return
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
                    // 启动阶段分三段显示：设置未加载显示启动页；无连接配置直接显示连接页；数据源服务准备好后再进入主壳。
                    RootNavTransition(
                        state = appState.mainSceneInitialPage,
                        enableAnimations = navigationConfig.enableAnimations
                    ) { content ->
                        when (content) {
                            AppStartupContent.CONNECTION -> {
                                // 首次打开直接渲染连接页，不创建 MainScreen/MainViewModel，避免拉起播放器等重依赖。
                                ConnectionScreen(connectionUiType = ConnectionUiType.FIRST_OPEN)
                            }
                            AppStartupContent.MAIN -> {
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
