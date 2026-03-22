package cn.xybbz.router

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import cn.xybbz.compositionLocal.LocalNavigator

@Composable
fun RouterCompose(
    paddingValues: PaddingValues,
    navigationState: NavigationState
) {
    val navigator = LocalNavigator.current
    SideEffect {
        Log.d("=====", "RouterCompose重组一次")
    }
    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
        sceneStrategy = remember { DialogSceneStrategy() },
        predictivePopTransitionSpec = RouterAnimate.DEFAULT.predictivePopTransitionSpec,
        transitionSpec = RouterAnimate.DEFAULT.transitionSpec,
        popTransitionSpec = RouterAnimate.DEFAULT.popTransitionSpec
    )

}