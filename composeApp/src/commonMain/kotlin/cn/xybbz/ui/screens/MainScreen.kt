/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import cn.xybbz.common.utils.Log
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.router.Connection
import cn.xybbz.router.Navigator
import cn.xybbz.router.OnDestinationChangedListener
import cn.xybbz.router.RootNavTransition
import cn.xybbz.router.RouterCompose
import cn.xybbz.router.platformNavigationConfig
import cn.xybbz.router.rememberNavigationState
import cn.xybbz.ui.components.AddPlaylistBottomComponent
import cn.xybbz.ui.components.AlertDialogComponent
import cn.xybbz.ui.components.BottomSheetCompose
import cn.xybbz.ui.components.LifecycleEffect
import cn.xybbz.ui.components.LoadingCompose
import cn.xybbz.ui.components.SnackBarPlayerComponent
import cn.xybbz.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun MainScreen(mainViewModel: MainViewModel = koinViewModel<MainViewModel>()) {

    val coroutineScope = rememberCoroutineScope()
    val ifOpenSelect by mainViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val ifConnectionConfig by mainViewModel.settingsManager.ifConnectionConfig.collectAsStateWithLifecycle()

    val navigationConfig = platformNavigationConfig
    val navigationState = rememberNavigationState(
        startRoute = navigationConfig.startRoute,
        topLevelRoutes = navigationConfig.topLevelRoutes
    )

    val navigator = remember {
        val navigator = Navigator(
            navigationState
        )
        navigator.addOnDestinationChangedListener(object : OnDestinationChangedListener {
            override fun onDestinationChanged(
                navigator: Navigator,
                destination: NavKey
            ) {
                mainViewModel.updateIfShowSnackBar(destination !is Connection)
            }
        })
        navigator.addOnDestinationChangedListener(object : OnDestinationChangedListener {
            override fun onDestinationChanged(
                navigator: Navigator,
                destination: NavKey
            ) {
                coroutineScope.launch(Dispatchers.Main.immediate) {
                    withFrameNanos {
                        if (ifOpenSelect) {
                            mainViewModel.selectControl.dismiss()
                        }
                    }

                }
            }

        })
        navigator
    }

    SideEffect {
        Log.d("=====", "MainScreen重组一次")
    }

    CompositionLocalProvider(
        LocalMainViewModel provides mainViewModel,
        LocalNavigator provides navigator
    ) {
        val mainViewModel = LocalMainViewModel.current

        //todo putDataSourceState 这个属性应该放在全局的object类里,不是放在mainViewModel里


        LifecycleEffect(
            onCreate = {
                Log.i("=====", "初始化")
            },
            onStart = {
                Log.i("=====", "创建")
                mainViewModel.putIterations(1)
            }, onDestroy = {
                Log.i("=====", "onDestroy")
                mainViewModel.putIterations(0)
//                mainViewModel.clearRemoteCurrent()
            }, onStop = {
                //后台
                Log.i("=====", "创建1")
                mainViewModel.putIterations(0)
            }, onPause = {
                //后台
                Log.i("=====", "创建2")

            }, onResume = {
                //todo 这里准备一次重新登陆,做成有间隔时间的重新登陆,不能每次重新打开就登陆
                Log.i("=====", "创建3")

            })

        AlertDialogComponent()
        BottomSheetCompose()

        AddPlaylistBottomComponent()
        MainScreenScaffold(
            navigationConfig = navigationConfig,
            navigationState = navigationState,
            navigator = navigator,
            snackbarHost = {
                SnackBarHostUi()
            }
        ) {
            RootNavTransition(
                state = !ifConnectionConfig,
                enableAnimations = navigationConfig.enableAnimations
            ) { bool ->
                if (bool) {
                    ConnectionScreen(connectionUiType = null)
                } else {
                    Box {
                        RouterCompose(
                            paddingValues = it,
                            navigationState = navigationState,
                            enableAnimations = navigationConfig.enableAnimations
                        )
                        LoadingCompose(modifier = Modifier.align(alignment = Alignment.Center))
                    }
                }
            }
        }
    }
}


@Composable
private fun SnackBarHostUi(modifier: Modifier = Modifier) {
    val mainViewModel = LocalMainViewModel.current
    val ifShowSnackBar by mainViewModel.settingsManager.ifShowSnackBar.collectAsStateWithLifecycle()
    if (ifShowSnackBar)
        Column(modifier = Modifier.then(modifier)) {
            SnackBarPlayerComponent(
                onClick = {
                    mainViewModel.putSheetState(true)
                })

        }
}
