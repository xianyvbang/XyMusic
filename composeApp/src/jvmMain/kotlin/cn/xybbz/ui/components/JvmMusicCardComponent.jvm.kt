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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.common.UiConstants.MusicCardImageSize
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.cover_suffix
import xymusic_kmp.composeapp.generated.resources.music_xy_placeholder_foreground

/**
 * JVM 桌面端音乐卡片，底部文字支持 hover 小手和变色。
 */
@Composable
internal fun JvmMusicCardComponent(
    modifier: Modifier = Modifier,
    id: String,
    name: String,
    artistName: String? = null,
    model: Any?,
    backModel: Any? = null,
    imageSize: Dp? = null,
    enabled: Boolean = true,
    brush: Brush = Brush.linearGradient(
        colors = listOf(Color(0xff3b82f6), Color(0xff8b5cf6)),
        start = Offset(x = Float.POSITIVE_INFINITY, y = 0f),
        end = Offset(x = 0f, y = Float.POSITIVE_INFINITY),
    ),
    shape: Shape,
    placeholder: DrawableResource? = Res.drawable.music_xy_placeholder_foreground,
    error: DrawableResource? = Res.drawable.music_xy_placeholder_foreground,
    fallback: DrawableResource? = Res.drawable.music_xy_placeholder_foreground,
    onRouter: (String) -> Unit,
) {
    Column(
        modifier = modifier.width(imageSize ?: MusicCardImageSize),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1F),
            enabled = enabled,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            onClick = composeClick {
                onRouter(id)
            },
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                XyImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush),
                    model = model,
                    backModel = backModel,
                    placeholder = placeholder,
                    error = error,
                    fallback = fallback,
                    contentDescription = "${name}${stringResource(Res.string.cover_suffix)}",
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))

        JvmMusicCardText(
            modifier = modifier,
            name = name,
            artistName = artistName ?: "",
            enabled = enabled,
            onClick = { onRouter(id) },
        )
    }
}

@Composable
private fun JvmMusicCardText(
    modifier: Modifier = Modifier,
    name: String,
    artistName: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        if (enabled) {
            XyText(
                text = name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                onClick = onClick,
            )
            XyTextSub(
                text = artistName,
                maxLines = 1,
                onClick = onClick,
            )
        } else {
            XyText(
                text = name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
            )
            XyTextSub(
                text = artistName,
                maxLines = 1,
            )
        }
    }
}
