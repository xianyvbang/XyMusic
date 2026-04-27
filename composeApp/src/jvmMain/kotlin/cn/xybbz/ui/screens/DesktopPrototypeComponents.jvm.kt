@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.xybbz.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.components.SongTable
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.xy.XyIconButton
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XySmallSlider
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xymusic_kmp.composeapp.generated.resources.*

/**
 * 标题 + 歌曲表格的组合区块。
 */
@Composable
internal fun SongSection(
    title: String?,
    songs: List<XyMusic>,
    showAlbumColumn: Boolean = true,
    showMetaColumn: Boolean = true,
    onOpenAlbum: () -> Unit = {},
    onOpenArtist: () -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        if (title != null) {
            TitleText(title)
        }
        SongTable(
            songs = songs,
            columns = SongTableColumns(
                showAlbumColumn = showAlbumColumn,
                showMetaColumn = showMetaColumn,
            ),
            metaText = ::prototypeSongMetaText,
            accentColor = { _, music -> prototypeSongAccent(music) },
            onOpenAlbum = { onOpenAlbum() },
            onOpenArtist = { onOpenArtist() },
        )
    }
}

/**
 * 标题 + 专辑卡片网格的组合区块。
 */
@Composable
internal fun AlbumSection(title: String, albums: List<AlbumCardData>, onOpenAlbum: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        TitleText(title)
        FixedGridSection(albums, onOpenAlbum)
    }
}

/**
 * 固定五列的卡片网格。
 */
@Composable
private fun FixedGridSection(albums: List<AlbumCardData>, onOpenAlbum: () -> Unit) {
    val rows = ((albums.size + 4) / 5).coerceAtLeast(1)
    val gridHeight = 252.dp * rows + 24.dp * (rows - 1)
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        userScrollEnabled = false,
        modifier = Modifier.fillMaxWidth().height(gridHeight),
    ) {
        items(albums) { album -> AlbumCard(album, onOpenAlbum) }
    }
}

/**
 * 搜索页热门卡片网格。
 */
@Composable
internal fun SearchCardGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        userScrollEnabled = false,
        modifier = Modifier.fillMaxWidth().height(144.dp),
    ) {
        items(trendingCards) { card -> SearchTrendCard(card) }
    }
}

/**
 * 搜索页单个彩色趋势卡片。
 */
@Composable
private fun SearchTrendCard(card: AlbumCardData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(card.highlight ?: card.accent)
            .aspectRatio(1.5f)
            .padding(16.dp),
    ) {
        Text(card.title, color = desktopColors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * 通用专辑/歌单/艺人卡片。
 */
@Composable
internal fun AlbumCard(card: AlbumCardData, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val background by animateColorAsState(if (hovered) desktopColors.bgHover else desktopColors.bgHighlight)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .hoverable(interactionSource)
            .debounceClickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box {
            CoverSwatch(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).shadow(12.dp, RoundedCornerShape(if (card.circular) 120.dp else 4.dp)),
                accent = card.accent,
                circular = card.circular,
                glyph = painterResource(if (card.circular) Res.drawable.person_24px else Res.drawable.album_24px),
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = hovered,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomEnd),
            ) {
                Box(
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp).size(48.dp).clip(CircleShape).background(desktopColors.theme),
                    contentAlignment = Alignment.Center,
                ) {
                    ResourceIcon(Res.drawable.play_arrow_24px, null, Color.Black)
                }
            }
        }
        Text(card.title, color = desktopColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(card.subtitle, color = desktopColors.textSecondary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * 从右侧滑出的播放队列面板。
 */
@Composable
internal fun QueuePanel(visible: Boolean, modifier: Modifier = Modifier, onClose: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally { it } + fadeIn(),
        exit = slideOutHorizontally { it } + fadeOut(),
        modifier = modifier.fillMaxHeight(),
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight()
                .background(desktopColors.bgElevated)
                .border(1.dp, desktopColors.divider)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("播放队列", color = desktopColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                ResourceIcon(Res.drawable.close_24px, null, desktopColors.textSecondary, modifier = Modifier.clickable(onClick = onClose))
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("正在播放", color = desktopColors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                QueueSongCard(recommendedSongs.first(), true)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("接下来播放", color = desktopColors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                queueSongs.forEach { song -> QueueSongCard(song, false) }
            }
        }
    }
}

/**
 * 播放队列中的单条歌曲卡片。
 */
@Composable
private fun QueueSongCard(song: XyMusic, active: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(if (active) Color(0x19FFFFFF) else Color.Transparent).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CoverSwatch(Modifier.size(40.dp), prototypeSongAccent(song))
        Column(modifier = Modifier.width(196.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(song.name, color = if (active) desktopColors.theme else desktopColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(song.artists?.firstOrNull().orEmpty(), color = desktopColors.textSecondary, fontSize = 12.sp)
        }
        if (active) {
            ResourceIcon(Res.drawable.volume_up_24px, null, desktopColors.theme, modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * 底部播放控制条。
 * 这里复用了现有的 XySmallSlider 和 XyIconButton。
 */
@Composable
internal fun PlayerBar(
    progress: Float,
    volume: Float,
    queueOpen: Boolean,
    onProgressChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleQueue: () -> Unit,
    onOpenAlbum: () -> Unit,
    onOpenArtist: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().height(90.dp).background(Color(0xD9000000)).border(1.dp, desktopColors.divider).padding(horizontal = 16.dp),
    ) {
        val sectionWidth = maxWidth / 3
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
        Row(
            modifier = Modifier.width(sectionWidth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CoverSwatch(Modifier.size(56.dp), prototypeSongAccent(recommendedSongs.first()))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Neon Lights", color = desktopColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onOpenAlbum))
                Text("The Synth Band", color = desktopColors.textSecondary, fontSize = 12.sp, modifier = Modifier.clickable(onClick = onOpenArtist))
            }
            ResourceIcon(Res.drawable.favorite_24px, null, desktopColors.theme)
        }

        Column(
            modifier = Modifier.width(sectionWidth).widthIn(max = 722.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                ResourceIcon(Res.drawable.shuffle_24px, null, desktopColors.textSecondary)
                ResourceIcon(Res.drawable.skip_previous_24px, null, desktopColors.textSecondary)
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(desktopColors.textPrimary), contentAlignment = Alignment.Center) {
                    ResourceIcon(Res.drawable.play_arrow_24px, null, Color.Black, modifier = Modifier.size(18.dp))
                }
                ResourceIcon(Res.drawable.skip_next_24px, null, desktopColors.textSecondary)
                ResourceIcon(Res.drawable.repeat_24px, null, desktopColors.textSecondary)
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1:12", color = desktopColors.textSecondary, fontSize = 12.sp)
                HoverSlider(progress, onProgressChange, Modifier.width(360.dp))
                Text("3:42", color = desktopColors.textSecondary, fontSize = 12.sp)
            }
        }

        Row(
            modifier = Modifier.width(sectionWidth),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ResourceIcon(Res.drawable.settings_voice_24px, null, desktopColors.textSecondary)
            ResourceIcon(
                Res.drawable.queue_music_24px,
                null,
                if (queueOpen) desktopColors.theme else desktopColors.textSecondary,
                modifier = Modifier.clickable(onClick = onToggleQueue),
            )
            ResourceIcon(Res.drawable.volume_up_24px, null, desktopColors.textSecondary)
            HoverSlider(volume, onVolumeChange, Modifier.width(90.dp))
        }
        }
    }
}

/**
 * 原型里用到的简化进度条封装。
 */
@Composable
private fun HoverSlider(value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    XySmallSlider(
        modifier = modifier,
        progress = value,
        onProgressChanged = onValueChange,
        progressBarColor = desktopColors.textPrimary,
        cacheProgressBarColor = desktopColors.theme.copy(alpha = 0.35f),
        backgroundBarColor = Color(0x4DFFFFFF),
        barHeight = 4f,
        thumbRadius = 4f,
    )
}

/**
 * 左侧导航菜单行。
 */
@Composable
internal fun HoverMenuRow(label: String, icon: DrawableResource, active: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val background by animateColorAsState(if (active) desktopColors.bgHover else Color.Transparent)
    val foreground by animateColorAsState(if (active || hovered) desktopColors.textPrimary else desktopColors.textSecondary)

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(background).hoverable(interactionSource).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ResourceIcon(icon, null, foreground)
        Text(label, color = foreground, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * 左侧歌单列表行。
 */
@Composable
internal fun HoverPlaylistRow(playlist: PlaylistCard, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val background by animateColorAsState(if (hovered) desktopColors.bgHover else Color.Transparent)
    val foreground by animateColorAsState(if (hovered) desktopColors.textPrimary else desktopColors.textSecondary)

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(background).hoverable(interactionSource).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CoverSwatch(Modifier.size(48.dp), playlist.accent)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(playlist.title, color = desktopColors.textPrimary, fontSize = 14.sp)
            Text(playlist.description, color = foreground, fontSize = 12.sp)
        }
    }
}

/**
 * 顶部栏圆形图标按钮。
 */
@Composable
internal fun CircularIconButton(icon: DrawableResource) {
    XyIconButton(
        onClick = {},
        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xB3000000)),
        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent),
    ) {
        ResourceIcon(icon, null, desktopColors.textPrimary, modifier = Modifier.size(18.dp))
    }
}

/**
 * 用纯色渐变模拟的封面占位块。
 */
@Composable
internal fun CoverSwatch(modifier: Modifier, accent: Color, circular: Boolean = false, glyph: Painter? = null) {
    Box(
        modifier = modifier.clip(if (circular) CircleShape else RoundedCornerShape(4.dp)).background(
            Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.55f)))
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (glyph != null) {
            XyImage(modifier = Modifier.size(if (circular) 64.dp else 32.dp), model = glyph, contentDescription = null)
        }
    }
}

/**
 * 基于资源图标的统一图标渲染封装。
 */
@Composable
internal fun ResourceIcon(resource: DrawableResource, contentDescription: String?, tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Image(
        modifier = modifier,
        painter = painterResource(resource),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(tint),
    )
}

/**
 * 详情页使用的绿色播放主按钮。
 */
@Composable
internal fun GreenPlayButton(size: Dp) {
    Box(modifier = Modifier.size(size).clip(CircleShape).background(desktopColors.theme), contentAlignment = Alignment.Center) {
        ResourceIcon(Res.drawable.play_arrow_24px, null, Color.Black, modifier = Modifier.size(24.dp))
    }
}

/**
 * 表格中的固定宽度单元格。
 */
@Composable
internal fun TableCell(text: String, width: Dp, color: Color, textAlign: TextAlign = TextAlign.Start, modifier: Modifier = Modifier) {
    Text(text = text, color = color, fontSize = 14.sp, textAlign = textAlign, modifier = modifier.width(width), maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/**
 * 页面大标题文本。
 */
@Composable
internal fun TitleText(text: String) {
    Text(text, color = desktopColors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
}

/**
 * 将详情头部模型转换为列表卡片模型，便于复用网格组件。
 */
internal fun DetailHeaderData.toAlbumCardData(): AlbumCardData = AlbumCardData(
    title = title,
    subtitle = subtitle.substringBefore("•").trim(),
    accent = accent,
)
