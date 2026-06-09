package cn.xybbz.ui.components

import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import cn.xybbz.config.image.rememberRawCoverUrls
import cn.xybbz.localdata.data.genre.XyGenre

/**
 * JVM 端专用的流派卡片重载，便于桌面页面直接传入实体对象。
 */
@Composable
fun MusicGenreCardComponent(
    modifier: Modifier = Modifier,
    genre: XyGenre?,
    imageSize: Dp? = null,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.shape,
    onRouter: (String) -> Unit,
) {
    val coverUrls = rememberRawCoverUrls(genre?.pic)

    JvmMusicCardComponent(
        modifier = modifier,
        id = genre?.itemId ?: "",
        name = genre?.name ?: "",
        imageSize = imageSize,
        model = coverUrls.primaryUrl,
        backModel = coverUrls.fallbackUrl,
        enabled = enabled,
        shape = shape,
        onRouter = onRouter,
    )
}
