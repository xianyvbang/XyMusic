package cn.xybbz.ui.ext

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier

expect fun Modifier.platformHoverClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
): Modifier
