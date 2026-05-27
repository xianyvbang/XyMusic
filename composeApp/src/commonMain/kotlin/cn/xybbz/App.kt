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
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.router.Navigator
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

@Composable
//@Preview
fun App(
    windowContentPadding: PaddingValues = PaddingValues(),
) {

    val settingsManager: SettingsManager = getKoin().get()
    // 启动状态单独由轻量 ViewModel 管理，避免首帧阶段提前创建 MainViewModel。
    val startupViewModel = koinViewModel<StartupViewModel>()
    val startupState by startupViewModel.uiState.collectAsState()
    // 连接配置只决定显示连接页还是主壳，不再等待自动登录完成。
    val ifConnectionConfig by settingsManager.ifConnectionConfig.collectAsState()
    val themeType by settingsManager.themeType.collectAsState()
    val imageFilePath by settingsManager.imageFilePath.collectAsState()
    val navigationConfig = platformNavigationConfig
    val navigationState = rememberNavigationState(
        startRoute = navigationConfig.startRoute,
        topLevelRoutes = navigationConfig.topLevelRoutes,
        enableTopLevelRoutes = navigationConfig.enableTopLevelRoutes
    )
    val navigator = remember(navigationState) {
        Navigator(navigationState)
    }

    val isDark = when (themeType) {
        ThemeTypeEnum.SYSTEM -> isSystemInDarkTheme()
        ThemeTypeEnum.DARK -> true
        ThemeTypeEnum.LIGHT -> false
    }

    LaunchedEffect(startupViewModel) {
        // Compose 首帧可以先进入主题和启动页，初始化任务在 ViewModel 内后台推进。
        startupViewModel.start()
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
                    // 启动阶段分三段显示：设置未加载显示启动页；无连接配置直接显示连接页；数据源服务准备好后再进入主壳。
                    if (!startupState.settingsLoaded) {
                        StartupScreen()
                    } else if (!ifConnectionConfig) {
                        // 首次打开直接渲染连接页，不创建 MainScreen/MainViewModel，避免拉起播放器等重依赖。
                        ConnectionScreen(connectionUiType = ConnectionUiType.FIRST_OPEN)
                    } else if (startupState.readyForContent) {
                        MainScreen(
                            navigationConfig = navigationConfig,
                            navigationState = navigationState,
                            navigator = navigator,
                        )
                    } else {
                        StartupScreen()
                    }
                    XyPopTipHost()
                }
            }
        }
    }
}
