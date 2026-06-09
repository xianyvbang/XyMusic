package cn.xybbz.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cn.xybbz.ui.xy.XyIconButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.ui.generated.resources.Res
import xymusic_kmp.ui.generated.resources.favorite_24px
import xymusic_kmp.ui.generated.resources.favorite_added
import xymusic_kmp.ui.generated.resources.favorite_border_24px
import xymusic_kmp.ui.generated.resources.favorite_removed

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FavoriteIconButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    enabled: Boolean = true,
    favoriteTint: Color = Color.Red,
    normalTint: Color? = null,
    contentDescription: String? = null,
    tooltip: String? = null,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
) {
    val defaultText = if (isFavorite) {
        stringResource(Res.string.favorite_added)
    } else {
        stringResource(Res.string.favorite_removed)
    }
    val buttonText = contentDescription ?: defaultText
    val tooltipText = tooltip ?: buttonText

    FavoriteIconButtonTooltip(tooltip = tooltipText) {
        XyIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource,
        ) {
            Icon(
                painter = painterResource(
                    if (isFavorite) {
                        Res.drawable.favorite_border_24px
                    } else {
                        Res.drawable.favorite_24px
                    }
                ),
                contentDescription = buttonText,
                modifier = iconModifier,
                tint = if (isFavorite) favoriteTint else normalTint ?: LocalContentColor.current,
            )
        }
    }
}

@Composable
internal expect fun FavoriteIconButtonTooltip(
    tooltip: String,
    content: @Composable () -> Unit,
)
