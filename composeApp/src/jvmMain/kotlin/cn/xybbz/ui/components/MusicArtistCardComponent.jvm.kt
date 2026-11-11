package cn.xybbz.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import cn.xybbz.common.constants.Constants
import cn.xybbz.config.image.rememberArtistCoverUrls
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.ui.theme.XyTheme
import org.jetbrains.compose.resources.stringResource

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
    val coverUrls = rememberArtistCoverUrls(artist)
    val name = artist?.name ?: stringResource(Constants.UNKNOWN_ARTIST)
    val textBitmapModel = textToBitmap(name)

    JvmMusicCardComponent(
        modifier = modifier,
        id = artist?.artistId ?: "",
        name = name,
        imageSize = imageSize,
        model = coverUrls.primaryUrl ?: textBitmapModel,
        backModel = coverUrls.fallbackUrl ?: textBitmapModel,
        brush = Brush.linearGradient(
            // 艺术家封面占位渐变从主题扩展颜色读取
            colors = listOf(
                XyTheme.extendedColors.artistCoverGradientStart,
                XyTheme.extendedColors.artistCoverGradientEnd,
            ),
            start = Offset(x = Float.POSITIVE_INFINITY, y = 0f),
            end = Offset(x = 0f, y = Float.POSITIVE_INFINITY),
        ),
        enabled = enabled,
        shape = shape,
        onRouter = onRouter,
    )
}
