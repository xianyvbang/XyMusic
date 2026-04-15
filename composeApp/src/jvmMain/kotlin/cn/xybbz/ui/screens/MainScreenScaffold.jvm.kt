package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.xybbz.router.NavigationState
import cn.xybbz.router.Navigator
import cn.xybbz.router.PlatformNavigationConfig
import cn.xybbz.router.jvmTopRouterDataList
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun MainScreenScaffold(
    modifier: Modifier,
    navigationConfig: PlatformNavigationConfig,
    navigationState: NavigationState,
    navigator: Navigator,
    snackbarHost: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = snackbarHost
    ) { paddingValues ->
        XyRow(paddingValues = PaddingValues()) {
            NavigationRail(
                modifier = Modifier.padding(paddingValues),
            ) {
                jvmTopRouterDataList.forEach { item ->
                    NavigationRailItem(
                        selected = navigator.state.topLevelRoute == item.route,
                        onClick = { navigator.navigate(route = item.route) },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = ""
                            )
                        },
                        label = { XyText(text = stringResource(item.title)) },
                    )
                }
            }
            content(paddingValues)
        }

    }
}
