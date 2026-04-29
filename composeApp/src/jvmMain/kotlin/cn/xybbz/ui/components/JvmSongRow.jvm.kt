package cn.xybbz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.common.utils.DateUtil
import cn.xybbz.config.image.rememberMusicCoverUrls
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.screens.desktopColors
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.favorite_24px
import xymusic_kmp.composeapp.generated.resources.favorite_border_24px
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.music_note_24px
import xymusic_kmp.composeapp.generated.resources.playlist_add_24px
import kotlin.math.absoluteValue

/**
 * 歌曲表格列开关配置。
 * 用于在首页、歌单详情、专辑详情等场景之间复用同一套行组件。
 */
internal data class SongTableColumns(
    val showFavoriteColumn: Boolean = false,
    val showInlineActions: Boolean = false,
    val showAlbumColumn: Boolean = true,
    val showMetaColumn: Boolean = true,
    val showDurationColumn: Boolean = true,
)

/**
 * 歌曲表格使用的固定宽度常量。
 * 统一放在这里，避免表头和行内容出现对不齐。
 */
internal object SongTableDefaults {
    val titleWidth = 320.dp
    val favoriteWidth = 52.dp
    val actionsWidth = 120.dp
    val albumWidth = 240.dp
    val metaWidth = 140.dp
    val durationWidth = 72.dp
    val coverSize = 40.dp
    val actionButtonSize = 32.dp
    val actionIconSize = 24.dp
    val actionButtonSpacing = 8.dp
}

/**
 * 歌曲表格中的单行内容。
 * 组件直接使用 XyMusic，展示文案、收藏状态和播放状态通过参数覆盖，避免额外包装展示对象。
 */
@Composable
internal fun SongRow(
    music: XyMusic,
    index: Int,
    columns: SongTableColumns,
    ifFavorite: Boolean,
    ifPlay: Boolean,
    modifier: Modifier = Modifier,
    albumText: String = music.albumName.orEmpty(),
    metaText: String = "",
    durationText: String = DateUtil.millisecondsToTime(music.runTimeTicks),
    accentColor: Color = defaultSongAccentColor(index, music),
    onClick: () -> Unit = {},
    onOpenAlbum: () -> Unit = {},
    onOpenArtist: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val coverUrls = rememberMusicCoverUrls(music)
    val rowBackgroundColor = if (ifPlay) desktopColors.bgHover else Color.Transparent

    XyRow(
        modifier = modifier
            .height(XyTheme.dimens.itemHeight)
            .clip(RoundedCornerShape(XyTheme.dimens.outerVerticalPadding / 2))
            .background(rowBackgroundColor)
            .debounceClickable(
                interactionSource = interactionSource,
                onClick = onClick
            )
            .pointerHoverIcon(PointerIcon.Hand),
        paddingValues = PaddingValues(
            horizontal = XyTheme.dimens.innerHorizontalPadding,
            vertical = XyTheme.dimens.outerVerticalPadding + XyTheme.dimens.outerVerticalPadding / 2
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongTitleCell(
            music = music,
            accentColor = accentColor,
            coverUrl = coverUrls.primaryUrl,
            fallbackCoverUrl = coverUrls.fallbackUrl,
            ifPlay = ifPlay,
            onOpenArtist = onOpenArtist
        )
        if (columns.showFavoriteColumn) {
            SongFavoriteCell(
                isFavorite = ifFavorite,
                onClick = onFavoriteClick,
            )
        }
        if (columns.showInlineActions) {
            SongInlineActions(hovered = hovered)
        }
        if (columns.showAlbumColumn) {
            SongTableCell(
                text = albumText,
                width = SongTableDefaults.albumWidth,
                color = desktopColors.textSecondary,
                onClick = onOpenAlbum,
            )
        }
        if (columns.showMetaColumn) {
            SongTableCell(
                text = metaText,
                width = SongTableDefaults.metaWidth,
                color = desktopColors.textSecondary
            )
        }
        if (columns.showDurationColumn) {
            SongTableCell(
                text = durationText,
                width = SongTableDefaults.durationWidth,
                color = desktopColors.textSecondary,
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * 标题单元格，内部包含封面、歌名和艺人名称。
 */
@Composable
private fun SongTitleCell(
    music: XyMusic,
    accentColor: Color,
    coverUrl: String?,
    fallbackCoverUrl: String?,
    ifPlay: Boolean,
    onOpenArtist: () -> Unit,
) {
    Row(
        modifier = Modifier.width(SongTableDefaults.titleWidth),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.innerVerticalPadding),
    ) {
        SongCover(
            coverUrl = coverUrl,
            fallbackCoverUrl = fallbackCoverUrl,
            accent = accentColor,
        )
        Column(verticalArrangement = Arrangement.Center) {
            XyText(
                text = music.name,
                color = if (ifPlay) desktopColors.theme else desktopColors.textPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            )

            XyTextSub(
                text = music.artists?.joinToString().orEmpty(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                onClick = onOpenArtist,
            )
        }
    }
}

/**
 * 收藏状态按钮列。
 */
@Composable
private fun SongFavoriteCell(
    isFavorite: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.width(SongTableDefaults.favoriteWidth),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick, modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)) {
            Icon(
                painter = painterResource(
                    if (isFavorite) Res.drawable.favorite_24px else Res.drawable.favorite_border_24px
                ),
                contentDescription = null,
                modifier = Modifier.size(SongTableDefaults.actionIconSize),
                tint = if (isFavorite) desktopColors.theme else desktopColors.textSecondary,
            )
        }
    }
}

/**
 * 行内悬浮操作区。
 * 为了保持列宽稳定，区域始终占位，只在 hover 时显示按钮。
 */
@Composable
private fun SongInlineActions(hovered: Boolean) {
    Box(
        modifier = Modifier.width(SongTableDefaults.actionsWidth),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = hovered,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    SongTableDefaults.actionButtonSpacing,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HoverActionIcon(Res.drawable.download_24px)
                HoverActionIcon(Res.drawable.playlist_add_24px)
                HoverActionIcon(Res.drawable.info_24px)
            }
        }
    }
}

/**
 * 行内悬浮操作按钮的统一外观封装。
 * 本次仅保留视觉层，不接业务点击。
 */
@Composable
private fun HoverActionIcon(iconRes: DrawableResource) {
    IconButton(
        onClick = {},
        modifier = Modifier.size(SongTableDefaults.actionButtonSize)
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(SongTableDefaults.actionIconSize),
            tint = desktopColors.textSecondary,
        )
    }
}

/**
 * 歌曲封面展示。
 * 有可用图片时优先显示图片，没有时回退到渐变占位块和默认音符图标。
 */
@Composable
private fun SongCover(
    coverUrl: String?,
    fallbackCoverUrl: String?,
    accent: Color,
) {
    Box(
        modifier = Modifier
            .size(SongTableDefaults.coverSize)
            .clip(RoundedCornerShape(XyTheme.dimens.outerVerticalPadding / 2))
            .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.55f)))),
        contentAlignment = Alignment.Center,
    ) {
        if (coverUrl.isNullOrBlank() && fallbackCoverUrl.isNullOrBlank()) {
            Icon(
                painter = painterResource(Res.drawable.music_note_24px),
                contentDescription = null,
                tint = desktopColors.textPrimary,
            )
        } else {
            XyImage(
                modifier = Modifier.matchParentSize(),
                model = coverUrl,
                backModel = fallbackCoverUrl,
                contentDescription = null,
            )
        }
    }
}

/**
 * 表格中的固定宽度文本单元格。
 */
@Composable
internal fun SongTableCell(
    text: String,
    width: Dp,
    color: Color,
    textAlign: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Box(modifier = modifier.width(width), contentAlignment = Alignment.CenterStart) {
        XyText(
            text = text,
            color = color,
            modifier = Modifier,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
            ),
            textAlign = textAlign,
            onClick = onClick,
        )
    }

}

/**
 * 表头里用于给无标题列预留宽度的占位块。
 */
@Composable
internal fun SongTableSpacer(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}

internal fun defaultSongAccentColor(
    index: Int,
    music: XyMusic,
): Color {
    val palette = listOf(
        Color(0xFF3C4CE0),
        Color(0xFFE14C40),
        Color(0xFFB98B29),
        Color(0xFF267A6A),
        Color(0xFF6B419B),
        Color(0xFF467B52),
    )
    val paletteIndex = (music.itemId.hashCode().absoluteValue + index) % palette.size
    return palette[paletteIndex]
}
