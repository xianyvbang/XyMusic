package cn.xybbz.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.stringResource
import cn.xybbz.R
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.ItemTrailingContent

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

    ItemTrailingContent(
        modifier = modifier,
        name = name,
        subordination = subordination,
        favoriteState = false,
        ifDownload = false,
        imgUrl = imgUrl,
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
                            contentDescription = stringResource(R.string.modify_playlist_name)
                        )
                    }
                }
                removePlaylistClick?.let {
                    IconButton(onClick = composeClick {
                        it.invoke()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.RemoveCircleOutline,
                            contentDescription = stringResource(R.string.delete_playlist)
                        )
                    }
                }

            }
        }
    )
}