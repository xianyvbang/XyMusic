@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package cn.xybbz.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavKey
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.router.Connection
import cn.xybbz.router.Home
import cn.xybbz.router.Navigator
import cn.xybbz.router.OnDestinationChangedListener
import cn.xybbz.router.RouterCompose
import cn.xybbz.router.rememberNavigationState
import cn.xybbz.ui.components.AddPlaylistBottomComponent
import cn.xybbz.ui.components.AlertDialogComponent
import cn.xybbz.ui.components.BottomSheetCompose
import cn.xybbz.ui.components.LifecycleEffect
import cn.xybbz.ui.components.LoadingCompose
import cn.xybbz.ui.components.MusicBottomMenuComponent
import cn.xybbz.ui.components.SnackBarPlayerComponent
import cn.xybbz.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@ExperimentalPermissionsApi
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(UnstableApi::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel = hiltViewModel<MainViewModel>()) {
    val coroutineScope = rememberCoroutineScope()
   val ifOpenSelect by mainViewModel.selectControl.uiState.collectAsState()

    val navigationState = rememberNavigationState(
        startRoute = if (mainViewModel.connectionIsLogIn) Home else Connection(),
        topLevelRoutes = setOf(Home, Connection())
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
                mainViewModel.updateIfShowSnackBar(destination != Connection)
                val isSelected = destination != navigationState.topLevelRoute
                Log.i("route", "是否选择${isSelected}")
            }
        })
        navigator.addOnDestinationChangedListener(object : OnDestinationChangedListener {
            override fun onDestinationChanged(
                navigator: Navigator,
                destination: NavKey
            ) {
                coroutineScope.launch(Dispatchers.Main.immediate) {
                    withFrameNanos{
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
        MusicBottomMenuComponent(onAlbumRouter = { albumId ->
            navigator.navigate(AlbumInfo(albumId, MusicDataTypeEnum.ALBUM))
        })
        AddPlaylistBottomComponent()
        Scaffold(
            snackbarHost = {
                SnackBarHostUi()
            },
        ) {
            Box {
                RouterCompose(
                    paddingValues = it,
                    navigationState = navigationState
                )
                LoadingCompose(modifier = Modifier.align(alignment = Alignment.Center))

            }
        }
    }
}


@Composable
private fun SnackBarHostUi(modifier: Modifier = Modifier) {
    val mainViewModel = LocalMainViewModel.current
    LaunchedEffect(mainViewModel.ifShowSnackBar) {
        Log.i("=====","是否显示currentSnackBarHostScreen ${mainViewModel.ifShowSnackBar}")
    }
    if (mainViewModel.ifShowSnackBar)
        Column(modifier = Modifier.then(modifier)) {
            SnackBarPlayerComponent(
                onClick = {
                    mainViewModel.putSheetState(true)
                })

        }
}
