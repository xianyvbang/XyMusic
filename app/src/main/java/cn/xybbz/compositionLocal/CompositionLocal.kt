package cn.xybbz.compositionLocal

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import cn.xybbz.common.constants.Constants
import cn.xybbz.viewmodel.MainViewModel

val LocalMainViewModel =
    compositionLocalOf<MainViewModel> { error(Constants.COMPOSITION_LOCAL_ERROR) }

val LocalNavController =
    compositionLocalOf<NavHostController> { error(Constants.COMPOSITION_LOCAL_ERROR) }