package cn.xybbz.ui.components

import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import cn.xybbz.common.constants.Constants
import cn.xybbz.config.image.rememberAlbumCoverUrls
import cn.xybbz.localdata.data.album.XyAlbum
import org.jetbrains.compose.resources.stringResource

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
    val coverUrls = rememberAlbumCoverUrls(album)

    JvmMusicCardComponent(
        modifier = modifier,
        id = album?.itemId ?: "",
        name = album?.name ?: "",
        artistName = album?.artists ?: stringResource(Constants.UNKNOWN_ARTIST),
        imageSize = imageSize,
        model = coverUrls.primaryUrl ?: imageUrl,
        backModel = coverUrls.fallbackUrl,
        enabled = enabled,
        shape = shape,
        onRouter = onRouter,
    )
}
