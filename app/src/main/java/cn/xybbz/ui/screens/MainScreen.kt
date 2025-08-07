@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package cn.xybbz.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.PagItems
import cn.xybbz.router.RouterCompose
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.AddPlaylistBottomComponent
import cn.xybbz.ui.components.AlertDialogComponent
import cn.xybbz.ui.components.BottomSheetCompose
import cn.xybbz.ui.components.LifecycleEffect
import cn.xybbz.ui.components.LoadingCompose
import cn.xybbz.ui.components.MusicBottomMenuComponent
import cn.xybbz.ui.components.SnackBarPlayerComponent
import cn.xybbz.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState


@ExperimentalPermissionsApi
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(UnstableApi::class)
@Composable
fun MainScreen() {

    val navController = rememberNavController()
    SideEffect {
        Log.d("=====", "MainScreen重组一次")
    }


    val context = LocalContext.current

    CompositionLocalProvider(
        LocalMainViewModel provides hiltViewModel<MainViewModel>(/*factory = MainViewModelFactory(current)*/),
        LocalNavController provides navController
    ) {
        val mainViewModel = LocalMainViewModel.current

        //todo putDataSourceState 这个属性应该放在全局的object类里,不是放在mainViewModel里

        LifecycleEffect(
            onCreate = {
                mainViewModel.clearRemoteCurrent()
                Log.i("=====", "初始化")
            },
            onStart = {
                Log.i("=====", "创建")

                mainViewModel.putIterations(1)
            }, onDestroy = {
                Log.i("=====", "onDestroy")
                mainViewModel.putIterations(0)
                mainViewModel.clearRemoteCurrent()
            }, onStop = {
                //后台
                Log.i("=====", "创建1")
                mainViewModel.putIterations(0)
            }, onPause = {
                //后台
                Log.i("=====", "创建2")

            }, onResume = {
                Log.i("=====", "创建3")

            })

        AlertDialogComponent()
        BottomSheetCompose()
        MusicBottomMenuComponent(onAlbumRouter = { albumId ->
            navController.navigate(RouterConstants.AlbumInfo(albumId, MusicDataTypeEnum.ALBUM))
        })
        AddPlaylistBottomComponent()

        Scaffold(
            /*topBar = {
                TopAppBar(title = {
                    //todo 这里可以写入链接的部分信息
                    Text(
                        text = "欢迎回来",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.W900
                    )
                })
            },*/
            snackbarHost = {
                SnackBarHostUi()
            },
            /*bottomBar = {
                NavigationBarAnim()
            }*/
        ) { it ->
            Box() {
                RouterCompose(paddingValues = it)
                LoadingCompose(modifier = Modifier.align(alignment = Alignment.Center))
            }
        }
    }
}


@Composable
private fun SnackBarHostUi(modifier: Modifier = Modifier) {
    val mainViewModel = LocalMainViewModel.current
    val navController = LocalNavController.current
    val currentSnackBarHostScreen by navController.currentSnackBarHostScreen()
    if (!currentSnackBarHostScreen)
        Column(modifier = Modifier.then(modifier)) {
            SnackBarPlayerComponent(
                onClick = {
                    mainViewModel.putSheetState(true)
                })

        }
}

@Composable
private fun NavigationBarAnim() {
    val navController = LocalNavController.current
    val currentNavigationScreen by navController.currentNavigationScreen()
    SideEffect {
        Log.d("=====", "NavigationBarAnim重组一次")
    }
    Box(modifier = Modifier.fillMaxWidth()) {

        AnimatedVisibility(
            visible = currentNavigationScreen,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            NavigationBar(
                modifier = Modifier.height(70.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                PagItems.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(screen.route::class) } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xffFB6580),
                            selectedTextColor = Color(0xffFB6580)
                        ),
                        icon = {
                            screen.icon(true)
                        })
                }
            }
        }
        Spacer(
            modifier = Modifier
                .navigationBarsPadding()
                .background(NavigationBarDefaults.containerColor)
        )
    }
}

@Composable
private fun NavController.currentNavigationScreen(): MutableState<Boolean> {
    val currentScreen = remember { mutableStateOf(true) }
    LaunchedEffect(key1 = this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            currentScreen.value =
                destination.hierarchy.any { route ->
                    Log.i("=====", "是否隐藏底部 ${route.route}")
                    PagItems.any { route.hasRoute(it.route::class) }
                            && !route.hasRoute(RouterConstants.Connection::class)

                }

        }
        addOnDestinationChangedListener(listener)
    }
    return currentScreen
}

@Composable
private fun NavController.currentSnackBarHostScreen(): MutableState<Boolean> {
    val currentScreen = remember { mutableStateOf(true) }
    LaunchedEffect(key1 = this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            currentScreen.value =
                destination.hierarchy.any { route ->
                    route.hasRoute(RouterConstants.Connection::class)
                }

        }
        addOnDestinationChangedListener(listener)
    }
    return currentScreen
}