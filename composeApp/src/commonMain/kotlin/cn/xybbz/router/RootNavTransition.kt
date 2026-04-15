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
fun RootNavTransition(
    state: Boolean,
    enableAnimations: Boolean = true,
    content: @Composable (Boolean) -> Unit
) {
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
