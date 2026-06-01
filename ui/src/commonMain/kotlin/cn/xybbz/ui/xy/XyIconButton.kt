package cn.xybbz.ui.xy

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cn.xybbz.ui.ext.platformHoverClickable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun XyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    val buttonInteractionSource = interactionSource ?: remember { MutableInteractionSource() }

    IconButton(
        onClick = onClick,
        modifier = Modifier
//            .size(IconButtonDefaults.smallIconSize)
            .then(modifier)
            .platformHoverClickable(
                interactionSource = buttonInteractionSource,
                enabled = enabled,
            ),
        enabled = enabled,
        colors = colors,
        interactionSource = buttonInteractionSource,
        content = content
    )
}
