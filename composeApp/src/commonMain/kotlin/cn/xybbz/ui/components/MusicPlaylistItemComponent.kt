package cn.xybbz.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.xybbz.config.image.rememberRawCoverUrls
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XySmallImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.delete_24px
import xymusic.composeapp.generated.resources.delete_playlist
import xymusic.composeapp.generated.resources.edit_24px
import xymusic.composeapp.generated.resources.modify_playlist_name
import xymusic.composeapp.generated.resources.music_xy_placeholder_foreground
import cn.xybbz.ui.xy.XyIconButton as IconButton

/**
 * 歌单列表项
 *
 * 单独实现行布局（不再依赖 ItemTrailingContent），以便控制封面尺寸、文字粗细与超长截断方式。
 *
 * @param modifier 修饰符
 * @param name 歌单名称
 * @param subordination 次要信息（如歌曲数量）
 * @param imgUrl 歌单封面地址
 * @param enabled 是否可点击
 * @param backgroundColor 背景颜色
 * @param brush 背景渐变，传入时优先于 backgroundColor
 * @param picSize 封面尺寸
 * @param onClick 点击回调
 * @param removePlaylistClick 删除歌单回调
 * @param editPlaylistClick 编辑歌单名称回调
 */
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
    picSize: Dp = 32.dp,
    onClick: (() -> Unit)? = null,
    removePlaylistClick: (() -> Unit)? = null,
    editPlaylistClick: (() -> Unit)? = null
) {
    val coverUrls = rememberRawCoverUrls(imgUrl)

    Row(
        modifier = modifier
            .height(XyTheme.dimens.itemHeight)
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            // brush 不为空时使用渐变背景，否则使用纯色背景
            .then(
                if (brush != null) {
                    Modifier.background(brush)
                } else {
                    Modifier.background(backgroundColor)
                }
            )
            .combinedClickable(enabled = enabled, onClick = composeClick {
                onClick?.invoke()
            })
            .padding(horizontal = XyTheme.dimens.innerHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // 歌单封面：加载中/加载失败都展示占位图，避免出现空白
        XySmallImage(
            model = coverUrls.primaryUrl,
            backModel = coverUrls.fallbackUrl,
            size = picSize,
            contentDescription = name,
            placeholder = Res.drawable.music_xy_placeholder_foreground,
            error = Res.drawable.music_xy_placeholder_foreground,
            fallback = Res.drawable.music_xy_placeholder_foreground,
        )
        Spacer(modifier = Modifier.width(XyTheme.dimens.outerVerticalPadding))

        // 歌单名称与歌曲数量：名称占满剩余宽度，超长直接截断（不显示省略号）
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            XyText(
                modifier = Modifier.fillMaxWidth(),
                text = name,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                color = MaterialTheme.colorScheme.onSurface
            )
            subordination?.let {
                XyTextSub(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }

        // 行尾操作按钮：编辑、删除
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            editPlaylistClick?.let {
                IconButton(onClick = composeClick {
                    it.invoke()
                }) {
                    Icon(
                        painter = painterResource(Res.drawable.edit_24px),
                        contentDescription = stringResource(Res.string.modify_playlist_name)
                    )
                }
            }
            removePlaylistClick?.let {
                IconButton(onClick = composeClick {
                    it.invoke()
                }) {
                    Icon(
                        painter = painterResource(Res.drawable.delete_24px),
                        contentDescription = stringResource(Res.string.delete_playlist)
                    )
                }
            }
        }
    }
}
