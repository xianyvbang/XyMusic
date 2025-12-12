package cn.xybbz.router

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cn.xybbz.R
import cn.xybbz.ui.theme.XyTheme
import coil.compose.AsyncImage

/**
 * 首页使用
 */
inline fun <reified T : Any> NavGraphBuilder.homeComposable(noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit) {
    val homeRouterAnimate = RouterAnimate.HomeRouterAnimate
    composable<T>(
        enterTransition = homeRouterAnimate.enter, exitTransition = homeRouterAnimate.exit
    ) {
        Box {
            AsyncImage(
                model = XyTheme.brash.backgroundImageUri,
                contentDescription = stringResource(R.string.background_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            content(it)
        }

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
        Box {
            AsyncImage(
                model = XyTheme.brash.backgroundImageUri,
                contentDescription = stringResource(R.string.background_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            content(it)
        }
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
        Box {
            AsyncImage(
                model = XyTheme.brash.backgroundImageUri,
                contentDescription = stringResource(R.string.background_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            content(it)
        }
    }
}