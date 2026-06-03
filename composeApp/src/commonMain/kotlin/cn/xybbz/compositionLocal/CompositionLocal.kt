package cn.xybbz.compositionLocal

import androidx.compose.runtime.compositionLocalOf
import cn.xybbz.common.constants.Constants
import cn.xybbz.router.Navigator
import cn.xybbz.ui.state.PlayerChromeState
import cn.xybbz.viewmodel.MainViewModel

val LocalMainViewModel =
    compositionLocalOf<MainViewModel> { error(Constants.COMPOSITION_LOCAL_ERROR) }

/**
 * 播放器外壳状态的 CompositionLocal，用于在主壳下的播放器组件之间共享纯 UI 状态。
 */
val LocalPlayerChromeState =
    compositionLocalOf<PlayerChromeState> { error(Constants.COMPOSITION_LOCAL_ERROR) }

val LocalNavigator =
    compositionLocalOf<Navigator> { error(Constants.COMPOSITION_LOCAL_ERROR) }
