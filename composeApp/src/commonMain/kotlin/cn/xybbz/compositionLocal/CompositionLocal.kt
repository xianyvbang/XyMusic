package cn.xybbz.compositionLocal

import androidx.compose.runtime.compositionLocalOf
import cn.xybbz.common.constants.Constants
import cn.xybbz.router.Navigator
import cn.xybbz.viewmodel.MainViewModel

val LocalMainViewModel =
    compositionLocalOf<MainViewModel> { error(Constants.COMPOSITION_LOCAL_ERROR) }

val LocalNavigator =
    compositionLocalOf<Navigator> { error(Constants.COMPOSITION_LOCAL_ERROR) }