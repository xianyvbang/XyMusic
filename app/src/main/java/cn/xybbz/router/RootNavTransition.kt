package cn.xybbz.router

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable

@Composable
fun RootNavTransition(
    state: Boolean,
    content: @Composable (Boolean) -> Unit
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            slideInHorizontally { it / 3 } + fadeIn() togetherWith
                    slideOutHorizontally { -it / 3 } + fadeOut()
        }
    ) {
        content(it)
    }
}
