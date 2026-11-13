package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cn.xybbz.router.NavigationState
import cn.xybbz.router.Navigator
import cn.xybbz.router.PlatformNavigationConfig
import cn.xybbz.ui.theme.XyTheme

@Composable
actual fun MainScreenScaffold(
    modifier: Modifier,
    navigationConfig: PlatformNavigationConfig,
    navigationState: NavigationState,
    navigator: Navigator,
    snackbarHost: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = modifier) {
        // 背景图与遮罩只在主壳绘制一次，页面内不再重复绘制
        MainScreenBackgroundLayer(modifier = Modifier.fillMaxSize())
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = snackbarHost,
            containerColor = if (XyTheme.brash.backgroundImageUri.isNullOrBlank()) {
                MaterialTheme.colorScheme.background
            } else {
                Color.Transparent
            }
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}
