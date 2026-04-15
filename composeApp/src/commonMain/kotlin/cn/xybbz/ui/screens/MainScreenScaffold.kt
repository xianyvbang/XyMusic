package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import cn.xybbz.router.NavigationState
import cn.xybbz.router.Navigator
import cn.xybbz.router.PlatformNavigationConfig

@Composable
expect fun MainScreenScaffold(
    navigationConfig: PlatformNavigationConfig,
    navigationState: NavigationState,
    navigator: Navigator,
    snackbarHost: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
)
