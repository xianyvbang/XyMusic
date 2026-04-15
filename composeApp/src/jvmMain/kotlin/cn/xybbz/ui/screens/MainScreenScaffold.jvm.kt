package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cn.xybbz.router.NavigationState
import cn.xybbz.router.Navigator
import cn.xybbz.router.PlatformNavigationConfig
import cn.xybbz.router.jvmTopRouterDataList
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.close
import xymusic_kmp.composeapp.generated.resources.close_24px
import xymusic_kmp.composeapp.generated.resources.menu_open_24px

@Composable
actual fun MainScreenScaffold(
    modifier: Modifier,
    navigationConfig: PlatformNavigationConfig,
    navigationState: NavigationState,
    navigator: Navigator,
    snackbarHost: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val railState = rememberWideNavigationRailState()
    val coroutineScope = rememberCoroutineScope()
    val railExpanded = railState.currentValue == WideNavigationRailValue.Expanded

    Scaffold(
        modifier = modifier,
//        snackbarHost = snackbarHost
    ) { paddingValues ->
        XyRow(paddingValues = PaddingValues()) {
            WideNavigationRail(
                modifier = Modifier.padding(paddingValues),
                state = railState,
                header = {
                    WideNavigationRailItem(
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                if (railExpanded) {
                                    railState.collapse()
                                } else {
                                    railState.expand()
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    if (railExpanded) Res.drawable.close_24px
                                    else Res.drawable.menu_open_24px
                                ),
                                contentDescription = if (railExpanded) {
                                    stringResource(Res.string.close)
                                } else {
                                    "展开侧边栏"
                                }
                            )
                        },
                        label = {
                            XyText(text = if (railExpanded) "收起" else "展开")
                        },
                        railExpanded = railExpanded,
                    )
                },
            ) {
                jvmTopRouterDataList.forEach { item ->
                    WideNavigationRailItem(
                        selected = navigator.state.topLevelRoute == item.route,
                        onClick = { navigator.navigate(route = item.route) },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = ""
                            )
                        },
                        label = { XyText(text = stringResource(item.title)) },
                        railExpanded = railExpanded,
                    )
                }
            }
            content(paddingValues)
        }

    }
}
