package cn.xybbz.router

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable

@Composable
fun <T> RootNavTransition(
    state: T,
    enableAnimations: Boolean = true,
    content: @Composable (T) -> Unit
) {
    // 支持 Boolean、枚举等任意稳定状态，启动外层三态切换也可以复用同一套根导航动画。
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            if (enableAnimations) {
                slideInHorizontally { it / 3 } + fadeIn() togetherWith
                        slideOutHorizontally { -it / 3 } + fadeOut()
            } else {
                EnterTransition.None togetherWith ExitTransition.None
            }
        }
    ) {
        content(it)
    }
}
