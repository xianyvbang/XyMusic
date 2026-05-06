package cn.xybbz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.utils.DateUtil
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.image.rememberMusicCoverUrls
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.screens.desktopColors
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopTooltipIconButton
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.MusicBottomMenuViewModel
import cn.xybbz.viewmodel.SidebarPlaylistViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_to_next_play_success
import xymusic_kmp.composeapp.generated.resources.add_to_playlist
import xymusic_kmp.composeapp.generated.resources.album
import xymusic_kmp.composeapp.generated.resources.album_24px
import xymusic_kmp.composeapp.generated.resources.artist
import xymusic_kmp.composeapp.generated.resources.av_timer_24px
import xymusic_kmp.composeapp.generated.resources.chevron_right_24px
import xymusic_kmp.composeapp.generated.resources.double_speed
import xymusic_kmp.composeapp.generated.resources.download
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.keyboard_arrow_right_24px
import xymusic_kmp.composeapp.generated.resources.more_horiz_24px
import xymusic_kmp.composeapp.generated.resources.music_note_24px
import xymusic_kmp.composeapp.generated.resources.other
import xymusic_kmp.composeapp.generated.resources.person_24px
import xymusic_kmp.composeapp.generated.resources.play_arrow_24px
import xymusic_kmp.composeapp.generated.resources.play_next
import xymusic_kmp.composeapp.generated.resources.playback
import xymusic_kmp.composeapp.generated.resources.playlist_add_24px
import xymusic_kmp.composeapp.generated.resources.playlist_play_24px
import xymusic_kmp.composeapp.generated.resources.skip_head_tail
import xymusic_kmp.composeapp.generated.resources.song_info
import xymusic_kmp.composeapp.generated.resources.speed_24px
import xymusic_kmp.composeapp.generated.resources.timer_close
import xymusic_kmp.composeapp.generated.resources.visibility_24px
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
    val showSelectionColumn: Boolean = false,
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
    val selectionWidth = 52.dp
    val coverSize = 40.dp
    val actionButtonSize = 32.dp
    val actionIconSize = 24.dp
    val actionButtonSpacing = 8.dp
}

private val songFavoriteTint = Color(0xFFE94157)

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
    currentPlayingMusicIdFlow: Flow<String?>,
    modifier: Modifier = Modifier,
    albumText: String = defaultSongAlbumText(music),
    metaText: String = "",
    durationText: String = DateUtil.millisecondsToTime(music.runTimeTicks),
    accentColor: Color = defaultSongAccentColor(index, music),
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onOpenAlbum: () -> Unit,
    onOpenArtist: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit = {
        AddPlaylistBottomData(
            ifShow = true,
            musicInfoList = listOf(music.itemId),
        ).show()
    },
    onMoreClick: () -> Unit = {
        music.show()
    },
    onSelectionClick: (String) -> Unit = {},
    showViewArtistMenuItem: Boolean = true,
    showViewAlbumMenuItem: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val coverUrls = rememberMusicCoverUrls(music)
    val rowIfPlay = rememberSongRowPlaying(
        musicId = music.itemId,
        currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
    )
    val rowBackgroundColor = if (rowIfPlay) desktopColors.bgHover else Color.Transparent
    val menuItems = rememberSongRowContextMenuItems(
        music = music,
        onPlay = onClick,
        onOpenAlbum = onOpenAlbum,
        onOpenArtist = onOpenArtist,
        showViewArtistMenuItem = showViewArtistMenuItem,
        showViewAlbumMenuItem = showViewAlbumMenuItem,
    )

    JvmRightClickDropdownMenuBox(
        modifier = modifier
            .clip(RoundedCornerShape(XyTheme.dimens.outerVerticalPadding / 2)),
        menuModifier = Modifier.width(220.dp),
        itemDataList = { menuItems },
    ) {
        XyRow(
            modifier = Modifier
                .height(XyTheme.dimens.itemHeight)
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
                ifPlay = rowIfPlay,
                onOpenArtist = onOpenArtist
            )
            if (columns.showFavoriteColumn) {
                SongFavoriteCell(
                    isFavorite = ifFavorite,
                    onClick = onFavoriteClick,
                )
            }
            if (columns.showInlineActions) {
                SongInlineActions(
                    hovered = hovered,
                    onDownloadClick = onDownloadClick,
                    onAddToPlaylistClick = onAddToPlaylistClick,
                    onMoreClick = onMoreClick,
                )
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
            if (columns.showSelectionColumn) {
                SongSelectionCell(
                    isSelected = isSelected,
                    onClick = { onSelectionClick(music.itemId) },
                )
            }
        }
    }
}

/**
 * 歌曲行播放状态。
 *
 * 由行组件按自己的 musicId 订阅当前播放歌曲 ID，避免外层页面逐行计算播放状态。
 */
@Composable
private fun rememberSongRowPlaying(
    musicId: String,
    currentPlayingMusicIdFlow: Flow<String?>,
): Boolean {
    val ifPlay by remember(currentPlayingMusicIdFlow, musicId) {
        currentPlayingMusicIdFlow
            .map { currentPlayingMusicId -> currentPlayingMusicId == musicId }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(false)
    return ifPlay
}

@Composable
private fun rememberSongRowContextMenuItems(
    music: XyMusic,
    onPlay: () -> Unit,
    onOpenAlbum: () -> Unit,
    onOpenArtist: () -> Unit,
    showViewArtistMenuItem: Boolean,
    showViewAlbumMenuItem: Boolean,
    musicBottomMenuViewModel: MusicBottomMenuViewModel = koinViewModel<MusicBottomMenuViewModel>(),
    sidebarPlaylistViewModel: SidebarPlaylistViewModel = koinViewModel<SidebarPlaylistViewModel>(),
): List<MenuItemDefaultData> {
    val coroutineScope = rememberCoroutineScope()
    val playlists by sidebarPlaylistViewModel.playlists.collectAsStateWithLifecycle()
    val addToNextPlaySuccess = stringResource(Res.string.add_to_next_play_success)
    val viewSubItems = listOfNotNull(
        if (showViewArtistMenuItem) {
            songContextMenuItem(
                title = stringResource(Res.string.artist),
                iconRes = Res.drawable.person_24px,
                onClick = onOpenArtist,
            )
        } else {
            null
        },
        if (showViewAlbumMenuItem) {
            songContextMenuItem(
                title = stringResource(Res.string.album),
                iconRes = Res.drawable.album_24px,
                onClick = onOpenAlbum,
            )
        } else {
            null
        },
    )

    return listOfNotNull(
        songContextMenuItem(
            title = stringResource(Res.string.playback),
            iconRes = Res.drawable.play_arrow_24px,
            onClick = onPlay,
        ),
        songContextMenuItem(
            title = stringResource(Res.string.play_next),
            iconRes = Res.drawable.playlist_play_24px,
            onClick = {
                musicBottomMenuViewModel.addNextPlayer(music.itemId)
                MessageUtils.sendPopTip(addToNextPlaySuccess)
            },
        ),
        songContextMenuItem(
            title = stringResource(Res.string.add_to_playlist),
            iconRes = Res.drawable.playlist_add_24px,
            trailingIconRes = Res.drawable.chevron_right_24px,
            dismissOnClick = playlists.isEmpty(),
            subItems = playlists.map { playlist ->
                songContextMenuItem(
                    title = playlist.name,
                    iconRes = Res.drawable.playlist_add_24px,
                    showLeadingIcon = false,
                    onClick = {
                        coroutineScope.launch {
                            musicBottomMenuViewModel.dataSourceManager.saveMusicPlaylist(
                                playlistId = playlist.itemId,
                                musicIds = listOf(music.itemId),
                            )
                        }
                    },
                )
            },
            subMenuModifier = Modifier.width(220.dp),
            subMenuOffset = DpOffset(220.dp, 0.dp),
            onClick = {
                AddPlaylistBottomData(
                    ifShow = true,
                    musicInfoList = listOf(music.itemId),
                ).show()
            },
        ),
        songContextMenuItem(
            title = stringResource(Res.string.song_info),
            iconRes = Res.drawable.info_24px,
            onClick = { music.show(MusicBottomMenuInitialAction.SongInfo) },
        ),
        if (viewSubItems.isNotEmpty()) {
            songContextMenuItem(
                title = "查看",
                iconRes = Res.drawable.visibility_24px,
                trailingIconRes = Res.drawable.chevron_right_24px,
                dismissOnClick = false,
                subItems = viewSubItems,
                subMenuModifier = Modifier.width(180.dp),
                subMenuOffset = DpOffset(220.dp, 0.dp),
                onClick = {},
            )
        } else {
            null
        },
        songContextMenuItem(
            title = stringResource(Res.string.double_speed),
            iconRes = Res.drawable.speed_24px,
            onClick = { music.show(MusicBottomMenuInitialAction.DoubleSpeed) },
        ),
        songContextMenuItem(
            title = stringResource(Res.string.skip_head_tail),
            iconRes = Res.drawable.keyboard_arrow_right_24px,
            onClick = { music.show(MusicBottomMenuInitialAction.SkipBeginningAndEnd) },
        ),
        songContextMenuItem(
            title = stringResource(Res.string.timer_close),
            iconRes = Res.drawable.av_timer_24px,
            onClick = { music.show(MusicBottomMenuInitialAction.Timer) },
        ),
    )
}

private fun songContextMenuItem(
    title: String,
    iconRes: DrawableResource,
    trailingIconRes: DrawableResource? = null,
    dismissOnClick: Boolean = true,
    subItems: List<MenuItemDefaultData> = emptyList(),
    subMenuModifier: Modifier = Modifier,
    subMenuOffset: DpOffset = DpOffset(180.dp, 0.dp),
    showLeadingIcon: Boolean = true,
    onClick: () -> Unit,
): MenuItemDefaultData {
    return MenuItemDefaultData(
        title = title,
        leadingIcon = { SongContextMenuIcon(iconRes) },
        trailingIcon = trailingIconRes?.let { { SongContextMenuIcon(it) } },
        dismissOnClick = dismissOnClick,
        subItems = subItems,
        subMenuModifier = subMenuModifier,
        subMenuOffset = subMenuOffset,
        showLeadingIcon = showLeadingIcon,
        onClick = onClick,
    )
}

@Composable
private fun SongContextMenuIcon(iconRes: DrawableResource) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = desktopColors.textSecondary,
    )
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
    val artistsText = music.artists?.joinToString().orEmpty()
    val mediaText = getMusicMedia(music.codec, music.bitRate)

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
                text = "$mediaText $artistsText",
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
        FavoriteIconButton(
            isFavorite = isFavorite,
            onClick = onClick,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
            iconModifier = Modifier.size(SongTableDefaults.actionIconSize),
            favoriteTint = songFavoriteTint,
            normalTint = desktopColors.textSecondary,
        )
    }
}

/**
 * 选择按钮列。
 */
@Composable
private fun SongSelectionCell(
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.width(SongTableDefaults.selectionWidth),
        contentAlignment = Alignment.Center,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        )
    }
}

/**
 * 行内悬浮操作区。
 * 为了保持列宽稳定，区域始终占位，只在 hover 时显示按钮。
 */
@Composable
private fun SongInlineActions(
    hovered: Boolean,
    onDownloadClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
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
                HoverActionIcon(
                    iconRes = Res.drawable.download_24px,
                    tooltip = stringResource(Res.string.download),
                    onClick = onDownloadClick,
                )
                HoverActionIcon(
                    iconRes = Res.drawable.playlist_add_24px,
                    tooltip = stringResource(Res.string.add_to_playlist),
                    onClick = onAddToPlaylistClick,
                )
                HoverActionIcon(
                    iconRes = Res.drawable.more_horiz_24px,
                    tooltip = stringResource(Res.string.other),
                    onClick = onMoreClick,
                )
            }
        }
    }
}

/**
 * 行内悬浮操作按钮的统一外观封装。
 */
@Composable
private fun HoverActionIcon(
    iconRes: DrawableResource,
    tooltip: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    DesktopTooltipIconButton(
        tooltip = tooltip,
        onClick = composeClick { onClick() },
        enabled = enabled,
        modifier = Modifier.size(SongTableDefaults.actionButtonSize)
            .pointerHoverIcon(PointerIcon.Hand),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(SongTableDefaults.actionIconSize),
            tint = if (enabled) desktopColors.textSecondary else desktopColors.textSecondary.copy(
                alpha = 0.38f
            ),
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

internal fun defaultSongAlbumText(music: XyMusic): String {
    return music.albumName?.takeIf { it.isNotBlank() } ?: music.album
}
