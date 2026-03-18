package cn.xybbz.ui.placeholder.material3


import androidx.annotation.FloatRange
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.runtime.Composable
import cn.xybbz.ui.placeholder.PlaceholderDefaults
import cn.xybbz.ui.placeholder.PlaceholderHighlight
import cn.xybbz.ui.placeholder.fade
import cn.xybbz.ui.placeholder.shimmer

/**
 * Creates a [PlaceholderHighlight] which fades in an appropriate color, using the
 * given [animationSpec].
 *
 * @sample com.google.accompanist.sample.placeholder.DocSample_Material_PlaceholderFade
 *
 * @param animationSpec the [AnimationSpec] to configure the animation.
 */
@Deprecated(
    """
accompanist/placeholder is deprecated and the API is no longer maintained. 
We recommend forking the implementation and customising it to your needs. 
For more information please visit https://google.github.io/accompanist/placeholder
"""
)
@Composable
public fun PlaceholderHighlight.Companion.fade(
    animationSpec: InfiniteRepeatableSpec<Float> = PlaceholderDefaults.fadeAnimationSpec,
): PlaceholderHighlight = PlaceholderHighlight.fade(
    highlightColor = PlaceholderDefaults.fadeHighlightColor(),
    animationSpec = animationSpec,
)

/**
 * Creates a [PlaceholderHighlight] which 'shimmers', using a default color.
 *
 * The highlight starts at the top-start, and then grows to the bottom-end during the animation.
 * During that time it is also faded in, from 0f..progressForMaxAlpha, and then faded out from
 * progressForMaxAlpha..1f.
 *
 * @sample com.google.accompanist.sample.placeholder.DocSample_Material_PlaceholderShimmer
 *
 * @param animationSpec the [AnimationSpec] to configure the animation.
 * @param progressForMaxAlpha The progress where the shimmer should be at it's peak opacity.
 * Defaults to 0.6f.
 */
@Deprecated(
    """
accompanist/placeholder is deprecated and the API is no longer maintained. 
We recommend forking the implementation and customising it to your needs. 
For more information please visit https://google.github.io/accompanist/placeholder
"""
)
@Composable
public fun PlaceholderHighlight.Companion.shimmer(
    animationSpec: InfiniteRepeatableSpec<Float> = PlaceholderDefaults.shimmerAnimationSpec,
    @FloatRange(from = 0.0, to = 1.0) progressForMaxAlpha: Float = 0.6f,
): PlaceholderHighlight = PlaceholderHighlight.shimmer(
    highlightColor = PlaceholderDefaults.shimmerHighlightColor(),
    animationSpec = animationSpec,
    progressForMaxAlpha = progressForMaxAlpha,
)