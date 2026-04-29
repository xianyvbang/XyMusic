package cn.xybbz.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import cn.xybbz.localdata.data.artist.XyArtist

/**
 * JVM 端专用的艺术家卡片重载，便于桌面页面直接传入实体对象。
 */
@Composable
fun MusicArtistCardComponent(
    modifier: Modifier = Modifier,
    artist: XyArtist?,
    imageSize: Dp? = null,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    onRouter: (String) -> Unit,
) {
    MusicArtistCardComponent(
        modifier = modifier,
        onItem = { artist },
        imageSize = imageSize,
        enabled = enabled,
        shape = shape,
        onRouter = onRouter,
    )
}
