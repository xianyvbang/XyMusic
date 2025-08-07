package cn.xybbz.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.xybbz.R
import cn.xybbz.common.constants.UiConstants.MusicCardImageSize
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyItemTextButton
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * 音乐卡片
 */
@OptIn(ExperimentalEncodingApi::class)
@Composable
fun MusicCardComponent(
    modifier: Modifier = Modifier,
    id: String,
    name: String,
    artistName: String? = null,
    model: Any?,
    imageSize: Dp? = null,
    brush: Brush = Brush.linearGradient(
        colors = listOf(Color(0xff3b82f6), Color(0xff8b5cf6)),
        start = Offset(x = Float.POSITIVE_INFINITY, y = 0f), // 右上角
        end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)   // 左下角
    ),
    shape: Shape,
    placeholder: Painter? = painterResource(id = R.drawable.music_xy_placeholder_foreground),
    error: Painter? = painterResource(id = R.drawable.music_xy_placeholder_foreground),
    fallback: Painter? = painterResource(id = R.drawable.music_xy_placeholder_foreground),
    onRouter: (String) -> Unit,
) {
    Column(
        modifier = modifier.width(imageSize ?: MusicCardImageSize),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1F),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            onClick = composeClick {
                onRouter(id)
            }) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                XyImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush
                        ),
                    model = model,
                    contentDescription = "${name}的封面",
                    placeholder = placeholder,
                    error = error,
                    fallback = fallback,
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        XyItemTextButton(modifier = Modifier, text = name, sub = artistName ?: "", onClick = {
            onRouter(id)
        })
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun MusicBackgroundCardComponent(
    modifier: Modifier = Modifier,
    id: String,
    name: String,
    artistName: String? = null,
    model: Any?,
    imageSize: Dp? = null,
    shape: Shape,
    backgroundColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    placeholder: Painter? = painterResource(id = R.drawable.music_xy_placeholder_foreground),
    error: Painter? = painterResource(id = R.drawable.music_xy_placeholder_foreground),
    fallback: Painter? = painterResource(id = R.drawable.music_xy_placeholder_foreground),
    onRouter: (String) -> Unit,
) {
    Column(
        modifier = modifier.width(imageSize ?: MusicCardImageSize),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1F),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            onClick = composeClick {
                onRouter(id)
            }) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                XyImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    model = model,
                    contentDescription = "${name}的封面",
                    placeholder = placeholder,
                    error = error,
                    fallback = fallback,
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        XyItemTextButton(modifier = Modifier, text = name, sub = artistName ?: "", onClick = {
            onRouter(id)
        })
    }
}


@Composable
fun MusicAlbumCardComponent(
    modifier: Modifier = Modifier,
    onItem: () -> XyAlbum?,
    imageSize: Dp? = null,
    imageUrl: String? = onItem()?.pic,
    shape: Shape = CardDefaults.shape,
    onRouter: (String) -> Unit,
) {

    val album by remember {
        mutableStateOf(onItem())
    }

    MusicCardComponent(
        modifier = modifier,
        id = album?.itemId ?: "",
        name = album?.name ?: "",
        artistName = album?.artists ?: "未知艺术家",
        imageSize = imageSize,
        model = imageUrl,
        shape = shape,
        onRouter = onRouter
    )
}


@Composable
fun MusicArtistCardComponent(
    modifier: Modifier = Modifier,
    onItem: () -> XyArtist?,
    imageSize: Dp? = null,
    shape: Shape = CircleShape,
    onRouter: (String) -> Unit,
) {

    val album by remember {
        mutableStateOf(onItem())
    }

    MusicCardComponent(
        modifier = modifier,
        id = album?.artistId ?: "",
        name = album?.name ?: "未知艺术家",
        imageSize = imageSize,
        model = textToBitmap(album?.name ?: "未知艺术家", album?.pic),
        brush = Brush.linearGradient(
            colors = listOf(Color(0xff10b981), Color(0xff06b6d4)),
            start = Offset(x = Float.POSITIVE_INFINITY, y = 0f), // 右上角
            end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)   // 左下角
        ),
        shape = shape,
        onRouter = onRouter
    )
}

@Composable
fun MusicMusicCardComponent(
    modifier: Modifier = Modifier,
    onItem: () -> XyMusic?,
    imageSize: Dp? = null,
    imageUrl: String? = onItem()?.pic,
    shape: Shape = CardDefaults.shape,
    onRouter: (String) -> Unit,
) {

    val music by remember {
        mutableStateOf(onItem())
    }

    MusicCardComponent(
        modifier = modifier,
        id = music?.itemId ?: "",
        name = music?.name ?: "",
        artistName = music?.artists ?: "未知艺术家",
        imageSize = imageSize,
        model = imageUrl,
        shape = shape,
        onRouter = onRouter
    )
}


@Composable
fun MusicGenreCardComponent(
    modifier: Modifier = Modifier,
    onItem: () -> XyGenre,
    imageSize: Dp? = null,
    shape: Shape = CardDefaults.shape,
    onRouter: (String) -> Unit,
) {

    val genre by remember {
        mutableStateOf(onItem())
    }

    MusicCardComponent(
        modifier = modifier,
        id = genre.itemId,
        name = genre.name,
        imageSize = imageSize,
        model = genre.pic,
        shape = shape,
        onRouter = onRouter
    )
}