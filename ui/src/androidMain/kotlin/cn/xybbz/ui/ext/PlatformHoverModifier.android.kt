package cn.xybbz.ui.ext

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier

actual fun Modifier.platformHoverClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
): Modifier = this
