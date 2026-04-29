package cn.xybbz.ui.components

import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import cn.xybbz.localdata.data.album.XyAlbum

/**
 * JVM 端专用的专辑卡片重载，便于桌面页面直接传入实体对象。
 */
@Composable
fun MusicAlbumCardComponent(
    modifier: Modifier = Modifier,
    album: XyAlbum?,
    imageSize: Dp? = null,
    imageUrl: String? = album?.pic,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.shape,
    onRouter: (String) -> Unit,
) {
    MusicAlbumCardComponent(
        modifier = modifier,
        onItem = { album },
        imageSize = imageSize,
        imageUrl = imageUrl,
        enabled = enabled,
        shape = shape,
        onRouter = onRouter,
    )
}
