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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import cn.xybbz.common.utils.Log
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.router.Connection
import cn.xybbz.router.NavigationState
import cn.xybbz.router.Navigator
import cn.xybbz.router.OnDestinationChangedListener
import cn.xybbz.router.PlatformNavigationConfig
import cn.xybbz.router.RootNavTransition
import cn.xybbz.router.RouterCompose
import cn.xybbz.ui.components.AddPlaylistBottomComponent
import cn.xybbz.ui.components.AlertDialogComponent
import cn.xybbz.ui.components.BottomSheetCompose
import cn.xybbz.ui.components.LifecycleEffect
import cn.xybbz.ui.components.LoadingCompose
import cn.xybbz.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun MainScreen(
    navigationConfig: PlatformNavigationConfig,
    navigationState: NavigationState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = koinViewModel<MainViewModel>(),
) {

    val coroutineScope = rememberCoroutineScope()
    val selectUiState by mainViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val ifConnectionConfig by mainViewModel.settingsManager.ifConnectionConfig.collectAsStateWithLifecycle()
    // 当前数据源服务对象，自动登录启动过程中会从 null 变为具体服务。
    val dataSourceServer by mainViewModel.dataSourceManager.dataSourceServerFlow.collectAsStateWithLifecycle()
    // 当前连接 ID 会先于服务对象写入，用于判断已有连接配置的初始化进度。
    val currentConnectionId by mainViewModel.dataSourceManager.currentConnectionId.collectAsStateWithLifecycle()
    // 有连接配置时，必须等数据源服务和连接 ID 都就绪，再创建标题栏/侧栏等会读取数据源的 UI。
    val dataSourceReady = dataSourceServer != null && currentConnectionId != null
    val currentSelectUiState = rememberUpdatedState(selectUiState)

    DisposableEffect(navigator, mainViewModel, coroutineScope) {
        val snackbarListener = object : OnDestinationChangedListener {
            override fun onDestinationChanged(
                navigator: Navigator,
                destination: NavKey
            ) {
                mainViewModel.updateIfShowSnackBar(destination !is Connection)
            }
        }
        val selectDismissListener = object : OnDestinationChangedListener {
            override fun onDestinationChanged(
                navigator: Navigator,
                destination: NavKey
            ) {
                coroutineScope.launch(Dispatchers.Main.immediate) {
                    withFrameNanos {
                        if (currentSelectUiState.value.isOpen) {
                            mainViewModel.selectControl.dismiss()
                        }
                    }
                }
            }
        }
        navigator.addOnDestinationChangedListener(snackbarListener)
        navigator.addOnDestinationChangedListener(selectDismissListener)
        onDispose {
            navigator.removeOnDestinationChangedListener(snackbarListener)
            navigator.removeOnDestinationChangedListener(selectDismissListener)
        }
    }

    SideEffect {
        Log.d("=====", "MainScreen重组一次")
    }

    CompositionLocalProvider(
        LocalMainViewModel provides mainViewModel,
    ) {
        val mainViewModel = LocalMainViewModel.current

        Box(modifier = modifier) {
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
            RootNavTransition(
                state = !ifConnectionConfig,
                enableAnimations = navigationConfig.enableAnimations
            ) { bool ->
                if (bool) {
                    ConnectionScreen(connectionUiType = null)
                } else if (!dataSourceReady) {
                    // 自动登录仍在后台准备数据源时先展示加载态，避免主界面抢先读取 DataSourceManager。
                    LoadingCompose(modifier = Modifier.align(alignment = Alignment.Center))
                } else {
                    MainScreenScaffold(
                        navigationConfig = navigationConfig,
                        navigationState = navigationState,
                        navigator = navigator,
                        snackbarHost = {
                            MainScreenSnackBarHost()
                        }
                    ) {
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
}
