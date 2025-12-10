@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package cn.xybbz.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ContentScale.Companion
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.rememberNavController
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.config.select.SelectControl
import cn.xybbz.localdata.enums.MusicDataTypeEnum
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
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@ExperimentalPermissionsApi
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(UnstableApi::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    SideEffect {
        Log.d("=====", "MainScreen重组一次")
    }

    CompositionLocalProvider(
        LocalMainViewModel provides hiltViewModel<MainViewModel>(),
        LocalNavController provides navController
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
                Log.i("=====", "创建3")

            })

        AlertDialogComponent()
        BottomSheetCompose()
        MusicBottomMenuComponent(onAlbumRouter = { albumId ->
            navController.navigate(RouterConstants.AlbumInfo(albumId, MusicDataTypeEnum.ALBUM))
        })
        AddPlaylistBottomComponent()
        navController.CurrentSelectChange(mainViewModel.selectControl)
        Scaffold(
            snackbarHost = {
                SnackBarHostUi()
            },
        ) {
            Box {
                /*Image(
                    painter = painterResource(R.drawable.artrist_info),
                    contentDescription = "背景图片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )*/

                /*AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )*/
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

@Composable
private fun NavController.CurrentSelectChange(selectControl: SelectControl) {
    val coroutineScope = rememberCoroutineScope()

    var needDismiss by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = this) {
        val listener = NavController.OnDestinationChangedListener { _, _, _ ->
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    if (selectControl.ifOpenSelect)
                        needDismiss = true
                }
            }
        }
        addOnDestinationChangedListener(listener)
    }

    if (needDismiss) {
        LaunchedEffect(true) {
            selectControl.dismiss()
            needDismiss = false
        }
    }
}
