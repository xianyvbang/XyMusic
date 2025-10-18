package cn.xybbz.router

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * 首页使用
 */
inline fun <reified T : Any> NavGraphBuilder.homeComposable(noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit) {
    val homeRouterAnimate = RouterAnimate.HomeRouterAnimate
    composable<T>(
        enterTransition = homeRouterAnimate.enter, exitTransition = homeRouterAnimate.exit
    ) {
        content(it)
    }
}

/**
 * 节点页面
 */
inline fun <reified T : Any> NavGraphBuilder.nodeComposable(noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit) {
    val nodeRouterAnimate = RouterAnimate.NodeRouterAnimate
    composable<T>(
        enterTransition = nodeRouterAnimate.enter,
        exitTransition = nodeRouterAnimate.exit,
        popEnterTransition = nodeRouterAnimate.popEnter,
        popExitTransition = nodeRouterAnimate.popExit
    ) {
        content(it)
    }
}

/**
 * 最内层页面
 */
inline fun <reified T : Any> NavGraphBuilder.extremityComposable(noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit) {
    val extremityRouterAnimate = RouterAnimate.ExtremityRouterAnimate
    composable<T>(
        enterTransition = extremityRouterAnimate.enter,
        exitTransition = extremityRouterAnimate.exit,
        popEnterTransition = extremityRouterAnimate.popEnter,
        popExitTransition = extremityRouterAnimate.popExit
    ) {
        content(it)
    }
}