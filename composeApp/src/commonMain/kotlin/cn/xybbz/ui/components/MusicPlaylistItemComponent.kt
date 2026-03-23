package cn.xybbz.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import cn.xybbz.config.image.rememberRawCoverUrls
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.ItemTrailingContent
import cn.xybbz.ui.xy.XyIconButton as IconButton

@Composable
fun MusicPlaylistItemComponent(
    modifier: Modifier = Modifier,
    name: String,
    subordination: String? = null,
    imgUrl: String?,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    brush: Brush? = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            Color.Gray.copy(alpha = 0.1f)
        ), tileMode = TileMode.Repeated
    ),
    onClick: (() -> Unit)? = null,
    removePlaylistClick: (() -> Unit)? = null,
    editPlaylistClick: (() -> Unit)? = null
) {
    val coverUrls = rememberRawCoverUrls(imgUrl)

    ItemTrailingContent(
        modifier = modifier,
        name = name,
        subordination = subordination,
        favoriteState = false,
        ifDownload = false,
        ifPlay = false,
        imgUrl = coverUrls.primaryUrl,
        backImgUrl = coverUrls.fallbackUrl,
        enabled = enabled,
        backgroundColor = backgroundColor,
        brush = brush,
        onClick = onClick,
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                editPlaylistClick?.let {
                    IconButton(onClick = composeClick {
                        it.invoke()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(Res.string.modify_playlist_name)
                        )
                    }
                }
                removePlaylistClick?.let {
                    IconButton(onClick = composeClick {
                        it.invoke()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.RemoveCircleOutline,
                            contentDescription = stringResource(Res.string.delete_playlist)
                        )
                    }
                }

            }
        }
    )
}

