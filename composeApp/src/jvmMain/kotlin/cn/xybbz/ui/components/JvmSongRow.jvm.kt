package cn.xybbz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.assembler.MusicPlayAssembler
import cn.xybbz.common.utils.DateUtil
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.image.rememberMusicCoverUrls
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.theme.jvmCoverAccentPalette
import cn.xybbz.ui.windows.DesktopTooltipBox
import cn.xybbz.ui.windows.DesktopTooltipIconButton
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.ui.xy.rememberSquareRequestSize
import cn.xybbz.viewmodel.SidebarPlaylistViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.*
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.add_to_next_play_success
import xymusic.composeapp.generated.resources.add_to_playlist
import xymusic.composeapp.generated.resources.album
import xymusic.composeapp.generated.resources.album_24px
import xymusic.composeapp.generated.resources.artist
import xymusic.composeapp.generated.resources.av_timer_24px
import xymusic.composeapp.generated.resources.chevron_right_24px
import xymusic.composeapp.generated.resources.double_speed
import xymusic.composeapp.generated.resources.download
import xymusic.composeapp.generated.resources.download_24px
import xymusic.composeapp.generated.resources.info_24px
import xymusic.composeapp.generated.resources.keyboard_arrow_right_24px
import xymusic.composeapp.generated.resources.more_horiz_24px
import xymusic.composeapp.generated.resources.music_note_24px
import xymusic.composeapp.generated.resources.other
import xymusic.composeapp.generated.resources.person_24px
import xymusic.composeapp.generated.resources.play_arrow_24px
import xymusic.composeapp.generated.resources.play_next
import xymusic.composeapp.generated.resources.playback
import xymusic.composeapp.generated.resources.playlist_add_24px
import xymusic.composeapp.generated.resources.playlist_play_24px
import xymusic.composeapp.generated.resources.skip_head_tail
import xymusic.composeapp.generated.resources.song_info
import xymusic.composeapp.generated.resources.speed_24px
import xymusic.composeapp.generated.resources.timer_close
import xymusic.composeapp.generated.resources.visibility_24px
import kotlin.math.absoluteValue

/**
 * 歌曲表格列开关配置。
 * 用于在首页、歌单详情、专辑详情等场景之间复用同一套行组件。
 */
internal data class SongTableColumns(
    val showFavoriteColumn: Boolean = false,
    val showInlineActions: Boolean = false,
    val showInlineDownloadButton: Boolean = true,
    val showAlbumColumn: Boolean = true,
    val showMetaColumn: Boolean = true,
    val showDurationColumn: Boolean = true,
    val showSelectionColumn: Boolean = false,
)

/**
 * 歌曲表格使用的固定宽度常量。
 * 统一放在这里，避免表头和行内容出现对不齐。
 * 标题列宽度按当前列表最长歌名测量后与所有行共享，不再用 weight 强占剩余空间；
 * 剩余空间交给专辑列吸收，其余固定列在保证内容可读的前提下尽量收窄。
 */
internal object SongTableDefaults {
    // 列表为空或尚未测量完成时的标题列默认宽度
    val titleWidth = 400.dp

    // 标题列最小宽度，保证封面和少量歌名可读
    val titleMinWidth = 220.dp

    // 标题列最大宽度，避免极长歌名把整张表撑开
    val titleMaxWidth = 480.dp

    // 专辑列默认宽度，未测量完成时使用
    val albumWidth = 180.dp

    // 专辑列最小宽度，保证少量专辑名可读
    val albumMinWidth = 140.dp

    // 专辑列最大宽度，避免专辑名过长时挤占悬浮操作列
    val albumMaxWidth = 240.dp

    val favoriteWidth = 40.dp
    val actionsWidth = 100.dp
    val metaWidth = 120.dp
    val durationWidth = 60.dp
    val selectionWidth = 48.dp
    val coverSize = 40.dp
    val actionButtonSize = 28.dp
    val actionIconSize = 22.dp
    val actionButtonSpacing = 6.dp
    val columnSpacing = 12.dp

    // 标题列测量时额外预留的余量，避免歌名刚好压线被省略号截断
    val titleExtraPadding = 8.dp
}

/**
 * 收藏爱心色，统一从主题扩展颜色读取，避免硬编码。
 */
private val songFavoriteTint: Color
    @Composable
    get() = XyTheme.extendedColors.favorite

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
    // 标题列宽度，由表头按当前列表最长歌名测量后传入，保证各行与表头对齐
    titleWidth: Dp = SongTableDefaults.titleWidth,
    // 专辑列宽度，同样由表头按当前列表最长专辑名测量后传入
    albumWidth: Dp = SongTableDefaults.albumWidth,
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
    // 背景色层级：选中 > 正在播放 > 悬停 > 透明，高优先级状态不会被悬停覆盖
    val rowBackgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            rowIfPlay -> MaterialTheme.colorScheme.surfaceContainerHighest
            hovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 120),
        label = "songRowBackground",
    )
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
                .jvmHoverDebounceClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            paddingValues = PaddingValues(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding + XyTheme.dimens.outerVerticalPadding / 2
            ),
            horizontalArrangement = Arrangement.spacedBy(SongTableDefaults.columnSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SongTitleCell(
                music = music,
                accentColor = accentColor,
                coverUrl = coverUrls.primaryUrl,
                fallbackCoverUrl = coverUrls.fallbackUrl,
                ifPlay = rowIfPlay,
                onOpenArtist = onOpenArtist,
                // 标题列按测量出的内容宽度固定，不再强占剩余空间
                modifier = Modifier.width(titleWidth),
            )
            if (columns.showFavoriteColumn) {
                SongFavoriteCell(
                    isFavorite = ifFavorite,
                    onClick = onFavoriteClick,
                )
            }
            // 收藏列之后的弹性空白
            Spacer(modifier = Modifier.weight(1f))
            if (columns.showInlineActions) {
                SongInlineActions(
                    hovered = hovered,
                    showDownloadButton = columns.showInlineDownloadButton,
                    onDownloadClick = onDownloadClick,
                    onAddToPlaylistClick = onAddToPlaylistClick,
                    onMoreClick = onMoreClick,
                )
            }
            // 操作列之后的弹性空白：与前一处分摊剩余空间，使操作列居中于收藏列与专辑列之间
            Spacer(modifier = Modifier.weight(1f))
            if (columns.showAlbumColumn) {
                SongAlbumCell(
                    text = albumText,
                    onClick = onOpenAlbum,
                    // 专辑列使用测量后的固定宽度
                    modifier = Modifier.width(albumWidth),
                )
            }
            if (columns.showMetaColumn) {
                SongTableCell(
                    text = metaText,
                    width = SongTableDefaults.metaWidth,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (columns.showDurationColumn) {
                SongTableCell(
                    text = durationText,
                    width = SongTableDefaults.durationWidth,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    dataSourceManager: DataSourceManager = koinInject(),
    musicPlayContext: MusicPlayContext = koinInject(),
    db: LocalDatabaseClient = koinInject(),
    downloadDb: DownloadDatabaseClient = koinInject(),
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
                coroutineScope.launch {
                    val playMusic = MusicPlayAssembler.attachFilePath(
                        playMusic = db.musicDao.selectExtendById(music.itemId),
                        downloadDb = downloadDb,
                        mediaLibraryId = dataSourceManager.getConnectionId().toString(),
                    )
                    playMusic?.let { musicPlayContext.addNextPlayer(it) }
                }
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
                            dataSourceManager.saveMusicPlaylist(
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
                title = stringResource(Res.string.jvm_song_row_text_01),
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
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
    modifier: Modifier = Modifier,
) {
    val artistsText = music.artists?.joinToString().orEmpty()
    val mediaText = getMusicMedia(music.codec, music.bitRate)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.innerVerticalPadding),
    ) {
        SongCover(
            coverUrl = coverUrl,
            fallbackCoverUrl = fallbackCoverUrl,
            accent = accentColor,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            XyText(
                text = music.name,
                color = if (ifPlay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
            iconModifier = Modifier.size(SongTableDefaults.actionIconSize),
            favoriteTint = songFavoriteTint,
            normalTint = MaterialTheme.colorScheme.onSurfaceVariant,
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
            modifier = Modifier.jvmHoverDebounceClickable(),
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
    showDownloadButton: Boolean,
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
                if (showDownloadButton) {
                    HoverActionIcon(
                        iconRes = Res.drawable.download_24px,
                        tooltip = stringResource(Res.string.download),
                        onClick = onDownloadClick,
                    )
                }
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
        modifier = Modifier.size(SongTableDefaults.actionButtonSize),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(SongTableDefaults.actionIconSize),
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
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
    // 行内封面按固定展示尺寸解码，避免按服务端原图解码拖慢列表渲染。
    val coverRequestSize = rememberSquareRequestSize(SongTableDefaults.coverSize)

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
                tint = MaterialTheme.colorScheme.onSurface,
            )
        } else {
            XyImage(
                modifier = Modifier.matchParentSize(),
                model = coverUrl,
                backModel = fallbackCoverUrl,
                requestSize = coverRequestSize,
                contentDescription = null,
            )
        }
    }
}

/**
 * 专辑单元格。
 * 宽度由外部传入，用于吸收表格剩余空间；
 * 专辑名称被截断时，鼠标移入展示完整名称提示。
 */
@Composable
private fun SongAlbumCell(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    // 仅当专辑名超出列宽时才开启提示，避免出现无意义的重复提示
    var hasOverflow by remember(text) { mutableStateOf(false) }

    val content: @Composable () -> Unit = {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = text,
                modifier = Modifier.jvmHoverDebounceClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
                color = if (hovered) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                onTextLayout = { textLayoutResult ->
                    hasOverflow = textLayoutResult.hasVisualOverflow
                },
            )
        }
    }

    if (hasOverflow && text.isNotBlank()) {
        DesktopTooltipBox(tooltip = text) {
            content()
        }
    } else {
        content()
    }
}

/**
 * 表格中的文本单元格。
 * width 为空时不限制宽度，由调用方通过 modifier（例如 weight）决定尺寸。
 */
@Composable
internal fun SongTableCell(
    text: String,
    width: Dp? = null,
    color: Color,
    textAlign: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.then(if (width == null) Modifier else Modifier.width(width)),
        contentAlignment = Alignment.CenterStart,
    ) {
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

/**
 * 按当前歌曲列表中最长的歌名计算标题列宽度。
 *
 * 只用列表前 200 首做测量，避免超长分页列表每次刷新都全量测量；
 * 宽度限制在 [SongTableDefaults.titleMinWidth] 与 [SongTableDefaults.titleMaxWidth] 之间，
 * 既保证歌名尽量完整显示，又不会因为个别超长歌名把整张表撑开。
 *
 * @param songs 当前表格已加载的歌曲列表。
 */
@Composable
internal fun rememberSongTableTitleWidth(songs: List<XyMusic>): Dp {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    // 与标题文本保持一致的字号和字重，避免测量结果偏窄导致省略号截断
    val titleTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
    )
    val coverSpacing = XyTheme.dimens.innerVerticalPadding
    // 用最长歌名的文本做缓存 key，列表变化但最长歌名不变时无需重复测量
    val longestName = remember(songs) {
        songs.take(200).maxByOrNull { music -> music.name.length }?.name.orEmpty()
    }
    return remember(longestName, textMeasurer, density, titleTextStyle, coverSpacing) {
        if (longestName.isBlank()) {
            return@remember SongTableDefaults.titleWidth
        }
        val textWidth = textMeasurer.measure(
            text = longestName,
            style = titleTextStyle,
            maxLines = 1,
        ).size.width
        with(density) {
            val width = textWidth.toDp() +
                SongTableDefaults.coverSize +
                coverSpacing +
                SongTableDefaults.titleExtraPadding
            width.coerceIn(SongTableDefaults.titleMinWidth, SongTableDefaults.titleMaxWidth)
        }
    }
}

/**
 * 按当前歌曲列表中最长的专辑名称计算专辑列宽度。
 *
 * 同样只测量前 200 首，并在 [SongTableDefaults.albumMinWidth] 与
 * [SongTableDefaults.albumMaxWidth] 之间夹紧，避免专辑名过长时挤占悬浮操作列；
 * 超出列宽的专辑名仍可通过鼠标悬停提示查看完整内容。
 *
 * @param albumNames 当前表格已加载歌曲的专辑名称列表。
 */
@Composable
internal fun rememberSongTableAlbumWidth(albumNames: List<String>): Dp {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    // 与专辑单元格文本保持一致的字号
    val albumTextStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
    // 用最长专辑名做缓存 key，列表变化但最长专辑名不变时无需重复测量
    val longestAlbumName = remember(albumNames) {
        albumNames.take(200).maxByOrNull { name -> name.length }.orEmpty()
    }
    return remember(longestAlbumName, textMeasurer, density, albumTextStyle) {
        if (longestAlbumName.isBlank()) {
            return@remember SongTableDefaults.albumWidth
        }
        val textWidth = textMeasurer.measure(
            text = longestAlbumName,
            style = albumTextStyle,
            maxLines = 1,
        ).size.width
        with(density) {
            val width = textWidth.toDp() + SongTableDefaults.titleExtraPadding
            width.coerceIn(SongTableDefaults.albumMinWidth, SongTableDefaults.albumMaxWidth)
        }
    }
}

internal fun defaultSongAccentColor(
    index: Int,
    music: XyMusic,
): Color {
    // 封面占位配色统一取桌面端常量，与明暗主题无关，避免列表滚动时颜色跳变
    val palette = jvmCoverAccentPalette
    val paletteIndex = (music.itemId.hashCode().absoluteValue + index) % palette.size
    return palette[paletteIndex]
}

internal fun defaultSongAlbumText(music: XyMusic): String {
    return music.albumName?.takeIf { it.isNotBlank() } ?: music.album
}
