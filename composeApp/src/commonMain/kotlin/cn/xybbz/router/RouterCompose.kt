package cn.xybbz.router

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import cn.xybbz.common.utils.Log
import cn.xybbz.compositionLocal.LocalNavigator
import com.github.panpf.sketch.cache.internal.EmptyMemoryCache.entries

@Composable
fun RouterCompose(
    paddingValues: PaddingValues,
    navigationState: NavigationState,
    enableAnimations: Boolean = true
) {
    val navigator = LocalNavigator.current
    val routerAnimate = if (enableAnimations) RouterAnimate.DEFAULT else RouterAnimate.NONE
    SideEffect {
        Log.d("=====", "RouterCompose重组一次")
    }
    NavDisplay(
        modifier = Modifier.padding(paddingValues),
        entries = navigationState.toEntries(platformEntryProvider),
        onBack = { navigator.goBack() },
        sceneStrategy = remember { DialogSceneStrategy() },
        predictivePopTransitionSpec = routerAnimate.predictivePopTransitionSpec,
        transitionSpec = routerAnimate.transitionSpec,
        popTransitionSpec = routerAnimate.popTransitionSpec
    )

}
