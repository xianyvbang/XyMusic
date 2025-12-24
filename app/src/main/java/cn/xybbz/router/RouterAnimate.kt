package cn.xybbz.router

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene

/**
 * 路由动画常量
 */
sealed class RouterAnimate(
    val enter: AnimatedContentTransitionScope<Scene<*>>.() -> ContentTransform?,
    val popExit: AnimatedContentTransitionScope<Scene<*>>.() -> ContentTransform?
) {
    /**
     * 最初节点
     */
    data object HomeRouterAnimate: RouterAnimate(
        enter = {
            EnterTransition.None togetherWith ExitTransition.None
        },
        popExit = {
            EnterTransition.None togetherWith ExitTransition.None
        }
    )

    /**
     * 节点UI动画
     */
    data object NodeRouterAnimate : RouterAnimate(
        enter = {
            fadeIn(animationSpec = tween(300, easing = LinearEasing)) +
                    slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    ) togetherWith ExitTransition.None
        },
        popExit = {
            EnterTransition.None togetherWith fadeOut(animationSpec = tween(300, easing = LinearEasing)) +
                    slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
        }
    )

    /**
     * 末端UI动画
     */
    data object ExtremityRouterAnimate:RouterAnimate(
        enter = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left
            ) togetherWith ExitTransition.None
        },
        popExit = {
            EnterTransition.None togetherWith  slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right
            )
        }
    )
}